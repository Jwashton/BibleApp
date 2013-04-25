package edu.southern.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/*
 * BEngineFileMover
 * Created by Michael Babienco
 * Copies into the data (lighthouse) folder ALL FILES in your assets folder 
 * (except for a few that you don't want)
 * for Mr. Dant's Bible Engine into the location DATA_FILE_PATH.
 * This is NOT the most optimized thing in the world, but it gets the job done.
 * Last Modified: February 12, 2013 by Michael Babienco
 */

public class BEngineFileMover {
	public String DATA_FILE_PATH = "/data/data/edu.southern/lighthouse/";
	public String DATABASE_FILE_PATH = "/data/data/edu.southern/databases/";
	private Context mContext;
	
	public BEngineFileMover(Context context) {
		mContext = context;
	}
	
	public boolean copyDatabaseFile() {
		File dbfile = new File(DATABASE_FILE_PATH);
		String filename = "dailyVerse.db";
		
		if (!dbfile.exists() || !dbfile.isDirectory()) {
			dbfile.mkdir();
		}
		AssetManager assetManager = mContext.getAssets();

		File f = new File(DATA_FILE_PATH + filename);
		if (!f.exists()) {
					try {
						InputStream in = null;
						OutputStream out = null;
						Log.d("BEngineFileMover", "Moving: " + filename);
						in = assetManager.open(filename);
						out = new FileOutputStream(DATA_FILE_PATH + filename);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
		
		
		return false;
	}
	

	/*
	 * copyDataFiles() The public function for users to call to check if the
	 * data files are in their correct location and copy them there if not.
	 * Returns true if it was successful, false otherwise.
	 */
	public boolean copyDataFiles() {
		copyDatabaseFile();
		return copyAndCheckDataFiles();
	}

	/*
	 * Checks to see that all the data files are available. If not, it will copy
	 * them into the correct location. Returns true if successful and false if
	 * not.
	 */
	private boolean copyAndCheckDataFiles() {
		File lighthouseDir = new File(DATA_FILE_PATH);
		if (!lighthouseDir.exists() || !lighthouseDir.isDirectory()) {
			lighthouseDir.mkdir();
		}
		AssetManager assetManager = mContext.getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		for (String filename : files) {
			if (!filename.equals("webkit") && !filename.equals("images")
					&& !filename.equals("sounds")
					&& !filename.equals("databases")) {
				InputStream in = null;
				OutputStream out = null;
				File f = new File(DATA_FILE_PATH + filename);
				if (!f.exists()) {
					try {
						Log.d("BEngineFileMover", "Moving: " + filename);
						in = assetManager.open(filename);
						out = new FileOutputStream(DATA_FILE_PATH + filename);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			}
		}
		boolean areAllFilesAvailable = validateFilesCopied();
		if (!areAllFilesAvailable)
			return false;
		else
			return true;
	}

	/*
	 * copyFile(InputStream in, OutputStream out) Copies the inputstream file to
	 * the outputstream location.
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	/*
	 * validateFilesCopied() Returns true if all data files are in the
	 * lighthouse directory. Returns false if even one file is missing.
	 */
	private boolean validateFilesCopied() {
		File lighthouseDir = new File(DATA_FILE_PATH);
		if (!lighthouseDir.exists() || !lighthouseDir.isDirectory()) {
			lighthouseDir.mkdir();
			return false;
		}
		File studyFiles = new File(DATA_FILE_PATH);
		File[] files = studyFiles.listFiles();
		boolean filesActive[] = { false, false, false, false, false, false,
				false, false, false, false, false, false, false, false, false,
				false, false, // KJV/main files
		};
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.equals("CINDEX.KJV"))
				filesActive[0] = true;
			else if (fileName.equals("CONCORD.KJV"))
				filesActive[1] = true;
			else if (fileName.equals("KJVLEX.DAT"))
				filesActive[2] = true;
			else if (fileName.equals("KJVLEX.LZW"))
				filesActive[3] = true;
			else if (fileName.equals("KJVLEX.NDX"))
				filesActive[4] = true;
			else if (fileName.equals("LZWTABLE.KJV"))
				filesActive[5] = true;
			else if (fileName.equals("SRCHSTRONGS.DAT"))
				filesActive[6] = true;
			else if (fileName.equals("SRCHSTRONGS.NDX"))
				filesActive[7] = true;
			else if (fileName.equals("STATS.KJV"))
				filesActive[8] = true;
			else if (fileName.equals("STRONGS.DAT"))
				filesActive[9] = true;
			else if (fileName.equals("STRONGS.NDX"))
				filesActive[10] = true;
			else if (fileName.equals("VERSE.KJV"))
				filesActive[11] = true;
			else if (fileName.equals("VINDEX.KJV"))
				filesActive[12] = true;
			else if (fileName.equals("WORDS.KJV"))
				filesActive[13] = true;
			else if (fileName.equals("WORDSNDX.KJV"))
				filesActive[14] = true;
			else if (fileName.equals("XREF.DAT"))
				filesActive[15] = true;
			else if (fileName.equals("XREF.NDX"))
				filesActive[16] = true;
		}
		for (int i = 0; i < filesActive.length; i++)
			if (!filesActive[i]) {
				Log.d("BEngineFileMover", "File: " + i + " not available!");
				return false;
			}
		return true;
	}
}
