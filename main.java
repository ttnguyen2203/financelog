import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main{
	public static void main(String[] args) throws IOException{
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(System.in));
			System.out.println("What is your username?: ");
			String name = reader.readLine();
			System.out.println("Hello " + name + ", " ); // name will be changed when user class
		  }

	public static void getFiles(string[] args) throws IOException{
			File folder = new File(" ");
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
				}else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
					 }
			  }
		}
}

