package com.sayan.logic;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PlaylistFolderGenerator {

	private static final String PLAYLIST_SRC_DIRECTORY = "F:\\Muzix\\Playlists";
	private static final String MP3_SRC_DIRECTORY = "F:\\Muzix";
	private static final String DESTINATION_DIRECTORY = "H:\\CAR_MUZIX";

	private static final int LOG_STRING_PADDING = 70;

	/**
	 * collect failed files
	 */
	static Map<String, String> errorFiles = new HashMap<>();

	public static void main(String args[]) {
		StopWatch timer = new StopWatch();
		timer.start();

		System.out.println("Playlist source folder : " + PLAYLIST_SRC_DIRECTORY);
		System.out.println("MP3 source folder : " + MP3_SRC_DIRECTORY);

		File playlistSrc = new File(PLAYLIST_SRC_DIRECTORY);

		displayPlaylistDir(playlistSrc);

		log.info("Playlist source folder : {}", PLAYLIST_SRC_DIRECTORY);
		log.info("Playlist source folder : {}", MP3_SRC_DIRECTORY);

		// for each playlist
		for (File playlistFile : Arrays.asList(playlistSrc.listFiles())) {

			System.out.println("\n\n" + "++++++++++++++++++++++++++++++++++++\n" + playlistFile.getName()
					+ "\n++++++++++++++++++++++++++++++++++++");

			// Read and Display playlist file
			//List<String> mp3List = displayPlayListContents(playlistFile);
			List<File> mp3FilesList  = transformWplFile(playlistFile);
			
			// create output dir for playlist
			File outputDir = new File(DESTINATION_DIRECTORY + "\\" + playlistFile.getName().replace(".wpl", ""));
			if (!outputDir.exists()) {
				outputDir.mkdirs();
				System.out.println("\nCreated destination directory! " + outputDir.getPath() + "\n");
			}
			else {
				System.out.println("\nDestination directory already exists! " + outputDir.getPath() + "\n");

			}


			// copy each mp3 to output dir
			for (File mp3File : mp3FilesList) {
				try {
					Path destMp3Path = Paths.get(outputDir.getAbsolutePath() + "\\" + mp3File.getName());

					System.out.print(
							"Copying..  " + StringUtils.rightPad(mp3File.getName(), LOG_STRING_PADDING) + " to  " + destMp3Path);
					Files.copy(Paths.get(mp3File.getAbsolutePath()), destMp3Path);
					System.out.print("\n");
				} catch (Exception e) {
					System.out.print("\t[X]\n");

					// ignore file exists
					if (!e.getClass().getName().equalsIgnoreCase("FileAlreadyExistsException")
							&& !errorFiles.containsKey(mp3File.getName())) {
						errorFiles.put(mp3File.getName(), e.getClass().getName());
					}
				}
			}

		}

		if (errorFiles.size() != 0) {
			System.out.println("\n\nFailed Files:\n=============");
			errorFiles.entrySet().forEach(entrySet -> System.out.println(
					" " + StringUtils.rightPad(entrySet.getKey(), LOG_STRING_PADDING) + "\t" + entrySet.getValue()));

			System.out.println("---------------------------------------------------Failed Count: " + errorFiles.size()
					+ "--------------------------------------------------");
		}
		timer.stop();

		long min = timer.getTime() / 1000 / 60;
		long sec = (timer.getTime() / 1000) % 60;
		long milli = timer.getTime() % 1000;

		System.out.println("\n\nCompleted Processing in : " + min + "m " + sec + "s " + milli + "ms ");

	}

	/**
	 * Displays playlist's content
	 * 
	 * @param playlistFile
	 * @return
	 */
	private static List<String> displayPlayListContents(File playlistFile) {
		Path path = Paths.get(playlistFile.getAbsolutePath());
		List<String> mp3List = new ArrayList<>();

		try {
			mp3List = Files.readAllLines(path);
		} catch (Exception e) {
			System.out.println("failed to read file: " + playlistFile.getName());
		}

		mp3List.forEach(mp3 -> System.out.println("\t" + mp3));
		return mp3List;
	}

	/**
	 * Displays playlists that were found
	 * 
	 * @param playlistSrc
	 */
	private static void displayPlaylistDir(File playlistSrc) {
		if (null != playlistSrc.list() && playlistSrc.list().length != 0) {
			System.out.println("Total " + playlistSrc.list().length + " list(s) found!! \n\nLists\n=====");
			Arrays.asList(playlistSrc.list()).forEach(src -> System.out.println("\t" + src));

			System.out.println("--EOL--");
		} else {
			System.out.println("PLAYLIST_SRC_DIRECTORY is empty!");
		}
	}

	private static List<File> transformWplFile(File wplFile) {

		List<File> mp3List = new ArrayList<>();
		String wplFileContent = "";

		Path wplFilePath = Paths.get(wplFile.getAbsolutePath());
		try {
			wplFileContent = Files.readString(wplFilePath);
		} catch (Exception e) {
			System.out.println("failed to read file: " + wplFile.getName());
		}

		// trim unnecessary bit
		int beginIndex = wplFileContent.indexOf("<seq>") + 7;
		int endIndex = wplFileContent.indexOf("</seq>");
		wplFileContent = wplFileContent.substring(beginIndex, endIndex);

		// replace generic stuff
		wplFileContent = wplFileContent.replaceAll("<media src=", "");
		wplFileContent = wplFileContent.replaceAll("/>", "");
		wplFileContent = wplFileContent.replaceAll("[a-zA-Z]+=\"\\{[^}]*\\}\"", "");

		String[] lines = wplFileContent.split(StringUtils.LF);
		for (String line : lines) {
			// replace ending line break
			line = line.replaceAll("\n", "");

			// replace all quotes
			line = line.replaceAll("\"", "");

			// replace all ..\ at start of address
			line = line.replaceAll("\\.\\.", "");

			mp3List.add(new File(MP3_SRC_DIRECTORY + line.trim()));
		}
		
		mp3List.forEach(mp3 -> System.out.println("\t" + mp3));

		return mp3List;
	}

}