package nl.sslleiden.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.youngmediaexperts.data.Duration;
import nl.youngmediaexperts.data.Marker;
import nl.youngmediaexperts.data.Timestamp;
import nl.youngmediaexperts.mux.Split;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.googlecode.mp4parser.FileDataSourceImpl;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
@Data
@Slf4j
public class MediaFile implements Comparable<MediaFile>, Serializable {
	
	private static final long serialVersionUID = -1488535239059545050L;
	
	private File file;
	private int camera;
	private int index;
	
	@Setter(AccessLevel.NONE)
	private Date creation;
	
	@Setter(AccessLevel.NONE)
	private List<Marker> markers = Lists.newArrayList();
	
	private Duration duration;
	
	private Timestamp inpoint, outpoint;
	
	/**
	 * No args constructor for serialization
	 */
	protected MediaFile() {}
	
	/**
	 * Construct a new {@code MediaFile}
	 * @param file
	 * @throws IOException
	 * @throws ParseException 
	 */
	public MediaFile(File file) throws IOException, ParseException {
		this.file = file;
		this.inpoint = new Timestamp(0,0,0);
		
		try(IsoFile isoFile = new IsoFile(new FileDataSourceImpl(file))) {
			// Parse the isoFile to get the bitrate and duration for the file
			MovieHeaderBox movieHeaderBox =
					isoFile.getMovieBox().getMovieHeaderBox();
			this.outpoint = this.duration =
					new Duration(movieHeaderBox.getDuration(), movieHeaderBox.getTimescale());
	        if(isoFile.getMovieBox().getSize() / duration.getTotalLength() > 1024)
	        	throw new RuntimeException("Bitrate for file is too high.");
		}
		
		String name = file.getName();
		parseFileName(name);
		
		markers.add(new Marker(new Timestamp(0,0,0) ,"Start"));
		log.info("Initialized file {}", this);
	}
	
	private static Pattern MARKER_PATTERN = Pattern.compile("\\t(\\d{2}:\\d{2}:\\d{2})\\s(.*)");
	private static Pattern DURATION_PATTERN = Pattern.compile("\\t\\d{2}:\\d{2}:\\d{2}");
	private static Pattern WHITESPACE_PATTERN = Pattern.compile("\\p{javaWhitespace}+");
	private static Pattern NEWLINE_PATTERN = Pattern.compile("(\\r?\\n)+");
	
	/**
	 * Construct a MediaFile from document
	 * @param scanner
	 * @throws ParseException 
	 */
	public MediaFile(CourseModel model, Scanner scanner) throws ParseException {
		String
			origName = scanner.next(),
			convName = scanner.next();
		/*	title = */ scanner.next().concat(scanner.nextLine());
		
		this.file = new File(model.getRootFolder(), convName);
		this.inpoint = new Timestamp(0,0,0);
		this.markers = Lists.newArrayList();
		parseFileName(origName);
		
		scanner.useDelimiter(NEWLINE_PATTERN);
		while(scanner.hasNext(MARKER_PATTERN)) {
			String next = scanner.next(MARKER_PATTERN);
			Matcher matcher = MARKER_PATTERN.matcher(next);
			if(matcher.matches()) {
				String
					timestamp = matcher.group(1),
					summary = matcher.group(2);
				this.markers.add(new Marker(new Timestamp(timestamp), summary));
			}
		}
		
		if(scanner.hasNext(DURATION_PATTERN)) {
			scanner.useDelimiter(WHITESPACE_PATTERN);
			this.outpoint = this.duration = new Duration(scanner.next());
		} else {
			throw new ParseException("Expected duration stamp", 0);
		}
		
		log.info("Initialized file {}", this);
	}
	
	private void parseFileName(String name) throws ParseException {
		Pattern pattern = Pattern.compile("^(\\d{2})_(\\d{4})_(\\d{4}-\\d{2}-\\d{2}_\\d{6}).mp4$");
		Matcher matcher = pattern.matcher(name);
		if(matcher.matches()) {
			// Try to parse the file name and try to fetch the 
			this.camera = Integer.parseInt(matcher.group(1));
			this.index = Integer.parseInt(matcher.group(2));
			
			String dateString = matcher.group(3);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmsss");
			this.creation = dateFormat.parse(dateString);
		} else {
			// Prevent null pointers exception, fill with a date
			try {
				this.index = Integer.parseInt(name.substring(0,2));
			} catch (NumberFormatException e)
				{};
		}
	}
	
	public String getFileName() {
		return file.getName();
	}
	
	public String getFileNameWithoutExtension() {
		return file.getName().replace(".mp4", "");
	}
	
	public String getSummary() {
		return markers.isEmpty() ? "" : markers.get(0).getDescription();
	}
	
	public String getPath() {
		return file.getPath();
	}
	
	@Override
	public int compareTo(MediaFile o) {
		int cmp = 0;
		if(creation != null && o.creation != null)
			cmp = (int) (creation.getTime() - o.creation.getTime());
		if(cmp == 0)
			cmp = index - o.index;
		if(cmp == 0)
			cmp = getFileName().compareTo(o.getFileName());
		return cmp;
	}

	/**
	 * Add a {@code Marker}
	 * @param marker
	 */
	public void addMarker(Marker marker) {
		this.markers.add(marker);
		Collections.sort(markers);
	}

	/**
	 * Remove a {@code Marker}
	 * @param marker
	 */
	public void removeMarker(Marker marker) {
		this.markers.remove(marker);
		Collections.sort(markers);
	}
	
	/**
	 * Clear all  {@code Markers}
	 */
	public void clearMarkers() {
		this.markers.clear();
	}
	
	/**
	 * Clean up {@code Markers} outside the in and outpoints for this {@code MediaFile}
	 */
	public void cleanUpMarkers() {
		List<Marker> toBeRemoved = Lists.newArrayListWithCapacity(markers.size());
		for(Marker marker : markers) {
			if(marker.getTimestamp().compareTo(inpoint) < 0 ||
				marker.getTimestamp().compareTo(outpoint) >= 0) {
				toBeRemoved.add(marker);
			}
		}
		this.markers.removeAll(toBeRemoved);
	}
	
	/**
	 * @return a copy of this {@code MediaFile}
	 */
	public MediaFile duplicate() {
		MediaFile copy = new MediaFile();
		copy.file = this.file;
		copy.camera = this.camera;
		copy.index = this.index;
		copy.creation = this.creation;
		copy.markers = Lists.newArrayList(this.markers);
		copy.duration = this.duration;
		return copy;
	} 
	
	/**
	 * Create a subclip from several {@code Markers}
	 * @param markers
	 * @return
	 */
	public MediaFile createSubClip(Marker[] markers) {
		assert markers != null && markers.length > 0;
		Marker
			inpoint = markers[0],
			outpoint = markers[markers.length-1];
	
		MediaFile newFile = duplicate();
		newFile.setInpoint(inpoint.getTimestamp());
		newFile.setOutpoint(getClostestEndpoint(outpoint));
		newFile.cleanUpMarkers();
		return newFile;
	}
	
	private Timestamp getClostestEndpoint(Marker inpoint) {
		int size = markers.size(),
			index = markers.indexOf(inpoint),
			lastIndex = index + 1;
		assert index != -1;
		if(lastIndex < size) {
			return markers.get(lastIndex).getTimestamp();
		} else {
			return duration;
		}
	}
	
	public boolean isTrimmable() {
		return !(this.getInpoint().equals(new Timestamp(0,0,0)) &&
				this.getOutpoint().equals(this.getDuration()));
	}
	
	public Date getCreation() {
		return creation == null ? new Date(file.lastModified()) : creation;
	}
	
	/**
	 * Trim the {@code MediaFile} to it's in and out points and shift the
	 * {@code Markers} accordingly
	 * @param callback
	 */
	public void trim(final FutureCallback<MediaFile> callback) {

		if(!isTrimmable()) {
			// There is no need to trim this
			return;
		}
		
		try {
			Split.trim(this, new FutureCallback<File>() {

				@Override
				public void onSuccess(File result) {
					MediaFile.this.setFile(result);
					
					// Update the markers
					Timestamp difference = MediaFile.this.getInpoint();
					List<Marker> oldMarkers = Lists.newArrayList(MediaFile.this.getMarkers());
					MediaFile.this.clearMarkers();
					for(Marker marker : oldMarkers) {
						MediaFile.this.addMarker(marker.subtract(difference));
					}
					
					// Update in and out points
					Timestamp outpoint = MediaFile.this.getOutpoint().subtract(difference); 
					MediaFile.this.setOutpoint(outpoint);
					MediaFile.this.setInpoint(new Timestamp(0,0,0));
					MediaFile.this.setDuration(new Duration(MediaFile.this.getOutpoint().subtract(difference)));
					
					// Run the callback
					callback.onSuccess(MediaFile.this);
				}

				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);
				}
				
			});
		} catch (Exception exception) {
			callback.onFailure(exception);
		}
	}
	
}
