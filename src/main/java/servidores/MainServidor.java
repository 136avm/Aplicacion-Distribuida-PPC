package servidores;

public class MainServidor {
	public static void main(String[] args) {
		Servidor servidor = new Servidor("s1", 9000);
		servidor.start();
		Servidor servidor2 = new Servidor("s2", 9001);
		servidor2.start();
		Servidor servidor3 = new Servidor("s3", 9002);
		servidor3.start();
		System.out.println("Servidor broadcast iniciado.");
	}
}
