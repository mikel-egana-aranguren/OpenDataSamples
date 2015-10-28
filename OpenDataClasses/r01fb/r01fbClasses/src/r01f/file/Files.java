package r01f.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import r01f.util.types.Strings;



public class Files {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Size units, from bytes to yotabytes.
     */
    public static final String[] SIZE_FORMAT_BYTES = {" b"," Kb"," Mb"," Gb"," Tb"," Pb"," Eb"," Zb"," Yb"};
	/**
     * Returns the file size with it's unit
     * <ul>
     * 		<li>If the file size is less than 1024 bytes: 'x b.'</li>
     * 		<li>If the file size is between 1024 bytes and 1048576 bytes :'x Kb.'</li>
     * 		<li>If the file size is between 1048576 bytes and 1073741824 bytes : 'x Mb.'</li>
     * 		<li>If the file size is greater than 1073741824 bytes: 'x Gb.'</li>
     * </ul>
     * @param fileBytes file size in bytes
     * @return the formatted file size 
     */
    public static String formatFileSize(final long fileBytes) {
        if (fileBytes <= 0) {
            return "";
        }
        // bytes
        if (fileBytes < 1024) {
            return fileBytes + SIZE_FORMAT_BYTES[0];
        }
        // incrementing "letter" while value >1023
        int i = 1;
        double d = fileBytes;
        while ((d = d / 1024) > (1024-1) ) {
            i++;
        }

        // remove symbols after coma, left only 2:
        /*long l = (long) (d * 100);
        d = (double) l / 100;*/

        d = Math.round(d*Math.pow(10,2))/Math.pow(10,2);

        if (i < SIZE_FORMAT_BYTES.length) {
            return d + SIZE_FORMAT_BYTES[i];
        }
        // if it reach here the value is big
        return String.valueOf(fileBytes);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the file name part
	 */
	public static String getName(final String fileName) {
		return Files.fileNameAndExtension(fileName)[0];
	}
	/**
	 * @return the file extension part
	 */
	public static String getExtension(final String fileName) {
		return Files.fileNameAndExtension(fileName)[1];
	}
	/**
	 * Splits a {@link String} with the file name and it's extension into a
	 * {@link String} array where the first entry is the file name (without extension)
	 * and the second entry is the extension (if the file has extension)
	 * @param fileName
	 * @return
	 */
	public static String[] fileNameAndExtension(final String fileName) {
		String[] outFileNameAndExtension = new String[2];
		if (Strings.isNOTNullOrEmpty(fileName)) {
			Pattern p = Pattern.compile("^(.*?)\\.?([^.]*?)$");
			Matcher m = p.matcher(fileName);
			if (m.matches()) {
				if (Strings.isNOTNullOrEmpty(m.group(1))) {
					outFileNameAndExtension[0] = m.group(1);
					outFileNameAndExtension[1] = m.group(2);
				} else {
					outFileNameAndExtension[0] = m.group(2);
				}
			}
		}
		return outFileNameAndExtension;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Appends a text to file
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	public static void appendToFile(final File file,
									final String data) throws IOException {
		// if file doesnt exists, then create it
    	if (!file.exists()) file.createNewFile();
    	
    	// true = append file
    	BufferedWriter bufferWritter = null;
    	try {
			FileWriter fileWritter = new FileWriter(file,
													true);		// append
	        bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write(data);
    	} finally {
    		if (bufferWritter != null) try {
    			bufferWritter.close();
    		} catch(IOException ioEx) {
    			// just ignore
    		}
    	}
	}
	/**
	 * Appends a binary data to file
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static void appendToFile(final File file,
									final byte[] data) throws IOException {
		// if file doesnt exists, then create it
    	if (!file.exists()) file.createNewFile();
    	
    	// true = append file
    	FileChannel channel = null;
    	try {
    		ByteBuffer buf = ByteBuffer.wrap(data);
			channel = new FileOutputStream(file,
										   true)		// append or ovewrite 
								.getChannel();
			// Writes a sequence of bytes to this channel from the given buffer.
			channel.write(buf);
    	} finally {
    		if (channel != null) try {
				// close the channel
				channel.close();
    		} catch(IOException ioEx) {
    			// just ignore
    		}
    	}
	}
}
