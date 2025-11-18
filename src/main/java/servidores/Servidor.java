package servidores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import mensajes.Control;
import mensajes.Estacion;

public class Servidor extends Thread {
    private DatagramSocket socket;
    private boolean running;
    private String nombre;
    private int puerto;
    private int frecuencia = 500;
    private boolean activo = true;
    private Estacion e = new Estacion();
    private String tipo = "X";
    private BufferedWriter jsonLog;
    private BufferedWriter xmlLog;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String[] variablesSeleccionadas = new String[3];
    private java.util.Random rnd = new java.util.Random();

    public Servidor(String nombre, int puerto) {
        this.nombre = nombre;
        this.puerto = puerto;
        
        seleccionarVariables();

        try {
        	File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            socket = new DatagramSocket(puerto);
            socket.setBroadcast(true);
            socket.setSoTimeout(500);
            jsonLog = new BufferedWriter(new FileWriter("logs/servidor_log.json", true));
            xmlLog = new BufferedWriter(new FileWriter("logs/servidor_log.xml", true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void seleccionarVariables() {
        String[] todas = {"T", "H", "PM10", "SO2", "NO2", "O3"};

        for (int i = 0; i < 3; i++) {
            int idx;
            do {
                idx = rnd.nextInt(todas.length);
            } while (contiene(variablesSeleccionadas, todas[idx]));

            variablesSeleccionadas[i] = todas[idx];
        }
    }

    private boolean contiene(String[] arr, String v) {
        for (String s : arr) {
            if (s != null && s.equals(v))
                return true;
        }
        return false;
    }
    
    private String generarDatosParciales() {
        e.generarValores(); // refresca valores

        if (tipo.equals("J"))
            return generarJsonParcial();
        else
            return generarXmlParcial();
    }
    
    private String generarJsonParcial() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (int i = 0; i < variablesSeleccionadas.length; i++) {
            String var = variablesSeleccionadas[i];
            sb.append("\"").append(var).append("\":")
              .append(obtenerValor(var));

            if (i < variablesSeleccionadas.length - 1)
                sb.append(",");
        }

        sb.append("}");
        return sb.toString();
    }
    
    private String generarXmlParcial() {
        StringBuilder sb = new StringBuilder();
        sb.append("<Estacion>");

        for (String var : variablesSeleccionadas) {
            sb.append("<").append(var).append(">")
              .append(obtenerValor(var))
              .append("</").append(var).append(">");
        }

        sb.append("</Estacion>");
        return sb.toString();
    }
    
    private Double obtenerValor(String var) {
        switch (var) {
            case "T": return e.getT();
            case "H": return e.getH();
            case "PM10": return e.getPM10();
            case "SO2": return e.getSO2();
            case "NO2": return e.getNO2();
            case "O3": return e.getO3();
            default: return null;
        }
    }
    
    private InetAddress obtenerBroadcast() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isLoopback() && ni.isUp()) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress broadcast = ia.getBroadcast();
                        if (broadcast != null) {
                            return broadcast;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public void run() {
        running = true;

        while (running) {
            try {
            	if(activo) {
            		StringBuilder sb = new StringBuilder();
            		String datos = generarDatosParciales();
            		sb.append(nombre).append(" ").append(puerto).append(" ").append(tipo).append(" ").append(datos);

            		String mensaje = sb.toString().trim();
            		byte[] buf = mensaje.getBytes();
	                InetAddress broadcastAddr = obtenerBroadcast();
	                if (broadcastAddr != null) {
	                    DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddr, 8000);
	                    socket.send(packet);
	                } else {
	                    System.out.println("No se pudo obtener la IP de broadcast, mensaje no enviado.");
	                }
            	}

            	try {
                    byte[] bufComando = new byte[256];
                    DatagramPacket cmdPacket = new DatagramPacket(bufComando, bufComando.length);
                    socket.receive(cmdPacket);
                    String recibido = new String(cmdPacket.getData(), 0, cmdPacket.getLength()).trim();
                    String[] partes = recibido.split(" ");
                    String tipo = partes[0];
                    
                    guardarEnLog(recibido, tipo);
                    
                    Control c = new Control();
                    if(tipo.equals("X")) {
                    	c.fromXmlString(recibido.substring(2));
                    } else if(tipo.equals("J")) {
                    	c.fromJsonString(recibido.substring(2));
                    }
                    String comandoStr = String.format(
                            "%s %s %s",
                            c.getComando(), c.getDestinatario(), c.getValor()
                        );
                    System.out.println("Comando recibido: " + comandoStr);

                    String cmd = c.getComando().toLowerCase();
                    String nombreServidor = c.getDestinatario();

                    if (nombreServidor.equals(this.nombre)) {
                        switch (cmd) {
                            case "cambiarfrecuencia":
                                int nuevaFrecuencia = Integer.parseInt(c.getValor());
                                this.frecuencia = nuevaFrecuencia - 500;
                                System.out.println("Frecuencia de " + nombre + " cambiada a " + nuevaFrecuencia + " ms");
                                break;
                            case "stop":
                                this.activo = false;
                                System.out.println("Servidor " + nombre + " detenido");
                                break;
                            case "start":
                                this.activo = true;
                                System.out.println("Servidor " + nombre + " reanudado");
                                break;
                            case "formato":
                        		String formato = c.getValor();
                        		if(formato.toLowerCase().equals("xml")) {
                        			this.tipo = "X";
                        			System.out.println("Formato de envio de " + nombre + " cambiado a XML");
                        		} else if(formato.toLowerCase().equals("json")) {
                        			this.tipo = "J";
                        			System.out.println("Formato de envio de " + nombre + " cambiado a JSON");
                        		}
                            	break;
                        }
                    }

                } catch (IOException e) {}

                Thread.sleep(frecuencia);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                running = false;
            }
        }

        socket.close();
    }
    
    private void guardarEnLog(String mensaje, String tipo) {
        String fecha = sdf.format(new Date());
        String lineaLog = "---- " + fecha + " ----\n" + mensaje.substring(2) + "\n\n";
        try {
            if(tipo.equals("J")) {
                jsonLog.write(lineaLog);
                jsonLog.flush();
            } else if(tipo.equals("X")) {
                xmlLog.write(lineaLog);
                xmlLog.flush();
            }
        } catch(IOException e) {
            System.err.println("Error escribiendo en el log: " + e.getMessage());
        }
    }
}