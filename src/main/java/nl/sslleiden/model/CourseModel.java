package nl.sslleiden.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.sslleiden.util.AbstractProposal;
import nl.sslleiden.util.Callback;
import nl.sslleiden.util.Proposal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class CourseModel implements Serializable {
	
	private static final long serialVersionUID = -5466292551728741440L;
	private static final String DEFAULT_DOCENT = "";
	
	private int periode = 1;
	private int jaartal = Calendar.getInstance().get(Calendar.YEAR);
	private String docent = DEFAULT_DOCENT;
	private Vak vak = Vak.OVERIG;
	private Niveau niveau = Niveau.ONBEKEND;
	private Cursus cursus = Cursus.OVERIG;
	
	@Setter(AccessLevel.NONE)
	private File rootFolder;
	
	@Setter(AccessLevel.NONE)
	private List<MediaFile> files;
	
	/**
	 * No args constructor for serialization
	 */
	protected CourseModel() {}
	
	/**
	 * Construct a new Course model based on a root folder
	 * @param root
	 * @throws IOException
	 * @throws ParseException 
	 */
	public CourseModel(File root) throws IOException, ParseException {
		files = Lists.newArrayList();
		this.rootFolder = root;
		
		if(root == null) {
			throw new RuntimeException("Specified root should be a folder");
		} else if ( root.isDirectory() ) {
			scanFolder(root);
		} else {
			scanFile(root);
		}
		
		Collections.sort(files);
		log.info("Created CourseModel {}", this);
	}
	
	private void scanFolder(File root) throws IOException, ParseException {
		for(File file : root.listFiles()) {
			if(file.getName().endsWith(".mp4")) {
				MediaFile asset = new MediaFile(file);
				files.add(asset);
			}
		}
	}
	
	private void scanFile(File file) throws FileNotFoundException, ParseException {
		try(Scanner scanner = new Scanner(file)) {
			this.rootFolder = file.getParentFile();
			this.files = Lists.newArrayList();
			scanner.next();
			this.vak = Vak.getCourseForStr(scanner.next().concat(scanner.nextLine()));
			this.cursus = Cursus.getTypeForStr(scanner.next());
			this.jaartal = scanner.nextInt();
			skip(scanner, "-");
			skip(scanner, "Periode");
			this.periode = 1;
			
			try {
				this.periode = Integer.parseInt(scanner.next());
			} catch ( NumberFormatException e ) {
				log.info("Failed to pase integer, using 1 instead", e);
			}
			
			this.niveau = Niveau.valueOf(scanner.next());
			scanner.next().concat(scanner.nextLine());
			this.docent = scanner.next().concat(scanner.nextLine());
			
			while(scanner.hasNext()) {
				if(scanner.hasNext(Pattern.compile(".+"))) {
					this.addMediaFile(new MediaFile(this, scanner));
				} else {
					scanner.next();
				}
			}
		}
	}
	
	private void skip(Scanner scanner, String expected) {
		String next = scanner.next();
		boolean check = next.equalsIgnoreCase(expected);
		assert check : String.format("Expected %s, but was %s", expected, next);
	}
	
	/**
	 * Add a {@code MediaFile} to this {@code CourseModel} and sort the list
	 * 
	 * @param mediaFile
	 */
	public void addMediaFile(MediaFile mediaFile) {
		files.add(mediaFile);
		Collections.sort(files);
	}
	
	/**
	 * Remove a {@code MediaFile} from this {@code CourseModel}
	 * @param mediaFile
	 */
	public void removeMediaFile(MediaFile mediaFile) {
		files.remove(mediaFile);
	}
	
	/**
	 * Generate sources
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public File generateSources() throws IOException, TemplateException {
		File file = new File(rootFolder, "index.html");
		if(!file.exists())
			file.createNewFile();
		
		File destination = new File(rootFolder, "vidplayer");
		if(!destination.exists())
			destination.mkdir();
		copyFolder(getResource("/vidplayer"), destination);
		
		Configuration configuration = new Configuration();
		configuration.setClassForTemplateLoading(this.getClass(), "/");
		Map<String, Object> data = ImmutableMap.<String, Object> of("model", this);
		Template template = configuration.getTemplate("index.ftl");
		
		try(Writer writer = new FileWriter (file)) {
			template.process(data, writer);
		}
		
		return file;
	}
	
	/**
	 * @param path
	 * @return get a resource File
	 */
	private File getResource(String path) {
		return new File(this.getClass().getResource(path).getPath());
	}
	
	/**
	 * Copy the dependencies to the right folder
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	private void copyFolder(File source, File destination) throws IOException {
		if(source.isDirectory()){
    		if(!destination.exists())
    			destination.mkdir();
    		//list all the directory contents
    		String files[] = source.list();
    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(source, file);
    		   File destFile = new File(destination, file);
    		   //recursive copy
    		   copyFolder(srcFile,destFile);
    		}
    	} else {
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		try(InputStream in = new FileInputStream(source);
    	        OutputStream out = new FileOutputStream(destination);) {
    			IOUtils.copy(in, out);
    		}
    	}
	}
	
	/**
	 * Export the model to a file
	 * @return created file
	 * @throws IOException
	 */
	public File saveToFile() throws IOException {
		File file = new File(rootFolder.getPath() + "/output.txt");
		if(!file.exists())
			file.createNewFile();
		saveToFile(file);
		return file;
	}

	/**
	 * Export the model to a file
	 * @param file
	 * @throws IOException
	 */
	public void saveToFile(File file) throws IOException {
		try(FileWriter writer = new FileWriter(file)) {	
			write(writer);
		}
	}
	
	/**
	 * Write this {@code CourseModel} to a {@code Writer}
	 * @param writer
	 * @throws IOException
	 */
	public void write(Writer writer) throws IOException {
		final String EOL = System.lineSeparator();
		writer.append("Trainingsvideo ").append(vak.getNaam()).append(EOL);
		writer.append(cursus.getOmschrijving()).append(' ').append(Integer.toString(jaartal))
			.append(" - periode ").append(Integer.toString(periode)).append(EOL);
		writer.append(niveau.getOmschrijving()).append(' ').append(vak.getNaam()).append(EOL);
		writer.append(docent).append(EOL);
		writer.append(EOL);
		
		for(MediaFile part : files) {
			part.write(writer);
		}
		
		writer.flush();
	}
	
	/**
	 * Clean up the root directory for this {@code CourseModel}
	 */
	public void cleanUpRoot(Callback<Proposal<List<File>>> callback) {
		callback.call(new AbstractProposal<List<File>>() {
			
			private final List<File> toBeRemoved = getFilesToBeRemoved();
			
			private List<File> getFilesToBeRemoved() {
				ImmutableList.Builder<File> builder = ImmutableList.<File> builder();
				if(rootFolder != null && rootFolder.isDirectory()) {
					FILES : for(File file : rootFolder.listFiles()) {
						for(MediaFile media : files) {
							if(file.equals(media.getFile())) {
								continue FILES;
							}
						}
						builder.add(file);
					}
				}
				return builder.build();
			}

			@Override
			public List<File> get() {
				return toBeRemoved;
			}

			@Override
			public void accept() {
				for(File file : toBeRemoved) {
					file.delete();
				}
			}
			
		});
	}
	
}
