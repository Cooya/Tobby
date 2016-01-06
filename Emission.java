import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;


public class Emission implements Runnable{
	private OutputStream out;

    private String  message = null;

    private Scanner sc = null;

    

    public Emission(OutputStream outputStream) {

        this.out = outputStream;

        

    }


    

    public void run() {

        

          sc = new Scanner(System.in);

          

          while(true){

                message = sc.nextLine();

                //out.write(arg0);

                //out.flush();

              }

    }
}
