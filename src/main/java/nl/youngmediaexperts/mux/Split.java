package nl.youngmediaexperts.mux;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.extern.slf4j.Slf4j;

import com.coremedia.iso.boxes.Container;
import com.google.common.util.concurrent.FutureCallback;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import nl.sslleiden.model.MediaFile;

@Slf4j
public final class Split {

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public static Future<File> split(final MediaFile mediaFile, final FutureCallback<File> callback) throws FileNotFoundException, IOException {
		final DataSource source = new FileDataSourceImpl(mediaFile.getFile());
		final Movie movie = MovieCreator.build(source);
		List<Track> tracks = movie.getTracks();
		movie.setTracks(new LinkedList<Track>());

		double startTime = (double) mediaFile.getInpoint().getTotalLength(),
				endTime = (double) mediaFile.getOutpoint().getTotalLength();

		boolean timeCorrected = false;

		// Here we try to find a track that has sync samples. Since we can
		// only start decoding
		// at such a sample we SHOULD make sure that the start of the new
		// fragment is exactly
		// such a frame
		for (Track track : tracks) {
			if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
				if (timeCorrected) {
					// This exception here could be a false positive in case
					// we have multiple tracks
					// with sync samples at exactly the same positions. E.g.
					// a single movie containing
					// multiple qualities of the same video (Microsoft
					// Smooth Streaming file)
					throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
				}
				startTime = correctTimeToSyncSample(track, startTime, false);
				endTime = correctTimeToSyncSample(track, endTime, true);
				timeCorrected = true;
			}
		}

		for (Track track : tracks) {
			double currentTime = 0, lastTime = 0;
			long currentSample = 0, startSample = -1, endSample = -1;

			for (int i = 0; i < track.getSampleDurations().length; i++) {
				long delta = track.getSampleDurations()[i];

				if (currentTime > lastTime && currentTime <= startTime) {
					// current sample is still before the new starttime
					startSample = currentSample;
				}
				if (currentTime > lastTime && currentTime <= endTime) {
					// current sample is after the new start time and still
					// before the new endtime
					endSample = currentSample;
				}
				lastTime = currentTime;
				currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
				currentSample++;
			}
			
			movie.addTrack(new CroppedTrack(track, startSample, endSample));
		}
		

		return executor.submit(new Callable<File>() {
			
			@Override
			public File call() throws FileNotFoundException, IOException {
				log.info("Start converting file {}", mediaFile);
		        Container out = new DefaultMp4Builder().build(movie);
				String path = String.format("%s%s%s-%s.mp4",
						mediaFile.getFile().getParent(),
						File.separator,
						mediaFile.getFileNameWithoutExtension(),
						System.currentTimeMillis());
				File destination = new File(path);
				if(destination.exists())
					throw new FileAlreadyExistsException(destination.toString() + " already exists!");
				else
					destination.createNewFile();
				log.info("Done converting, now writing to file: {}", destination);
				try(FileOutputStream fos = new FileOutputStream(destination);
					FileChannel fc = fos.getChannel()) {
					out.writeContainer(fc);
				} catch (Exception exception) {
					callback.onFailure(exception);
					throw exception;
				} finally {
					// We close the source here because otherwise autoclosable
					// closes it while it's still needed in the executor
					source.close();
				}
				log.info("Done writing file: {}", destination);
				callback.onSuccess(destination);
				return destination;
			}
			
		});
	}

	private static double correctTimeToSyncSample(Track track, double cutHere,
			boolean next) {
		double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
		long currentSample = 0;
		double currentTime = 0;
		for (int i = 0; i < track.getSampleDurations().length; i++) {
			long delta = track.getSampleDurations()[i];

			if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
				// samples always start with 1 but we start with zero therefore
				// +1
				timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(),
						currentSample + 1)] = currentTime;
			}
			currentTime += (double) delta
					/ (double) track.getTrackMetaData().getTimescale();
			currentSample++;

		}
		double previous = 0;

		for (double timeOfSyncSample : timeOfSyncSamples) {
			if (timeOfSyncSample > cutHere) {
				if (next) {
					return timeOfSyncSample;
				} else {
					return previous;
				}
			}
			previous = timeOfSyncSample;
		}

		return timeOfSyncSamples[timeOfSyncSamples.length - 1];
	}

}
