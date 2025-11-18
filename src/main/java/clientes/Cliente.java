package clientes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import mensajes.Control;
import mensajes.Estacion;

public class Cliente {
    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private Map<String, List<String>> servidores;
    private BufferedWriter jsonLog;
    private BufferedWriter xmlLog;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String tipo = "X";

    public Cliente() {
        this.servidores = new HashMap<String, List<String>>();
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            socket = new DatagramSocket(8000);
            jsonLog = new BufferedWriter(new FileWriter("logs/cliente_log.json", true));
            xmlLog = new BufferedWriter(new FileWriter("logs/cliente_log.xml", true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void escucharConCallback(Consumer<String> callback) {
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String[] mensaje = received.split(" ");
                String resultado = String.join(" ", Arrays.copyOfRange(mensaje, 3, mensaje.length));
                String tipo = mensaje[2];
                
                guardarEnLog(received, tipo);
                
                Estacion e = null;
                if(tipo.equals("X") && resultado.contains("<Estacion>")) {
                    e = new Estacion();
                    e.fromXmlString(resultado);
                } else if(tipo.equals("J") && resultado.startsWith("{")) {
                    e = new Estacion();
                    e.fromJsonString(resultado);
                }
                StringBuilder sb = new StringBuilder();
                if(e.getT() != null) sb.append("T: ").append(String.format("%.2f", e.getT())).append("; ");
                if(e.getH() != null) sb.append("H: ").append(String.format("%.2f", e.getH())).append("; ");
                if(e.getPM10() != null) sb.append("PM10: ").append(String.format("%.2f", e.getPM10())).append("; ");
                if(e.getSO2() != null) sb.append("SO2: ").append(String.format("%.2f", e.getSO2())).append("; ");
                if(e.getNO2() != null) sb.append("NO2: ").append(String.format("%.2f", e.getNO2())).append("; ");
                if(e.getO3() != null) sb.append("O3: ").append(String.format("%.2f", e.getO3())).append("; ");

                // Quitamos el último "; " si existe
                if(sb.length() >= 2) sb.setLength(sb.length() - 2);

                resultado = sb.toString();

                if (!servidores.keySet().contains(mensaje[0])) {
                	ArrayList<String> lista = new ArrayList<String>();
                	lista.add(mensaje[1]);
                	lista.add(packet.getAddress().toString());
                    servidores.put(mensaje[0], lista);
                }

                callback.accept("[Broadcast] " + resultado);
            } catch (IOException e) {
                callback.accept("Error: " + e.getMessage());
            } catch (Exception ex) {
                callback.accept("Paquete malformado: " + ex.getMessage());
            }
        }
    }

    public void mostrarServidoresConCallback(Consumer<String> callback) {
        if (servidores.isEmpty()) {
            callback.accept("No hay servidores registrados.");
            return;
        }

        servidores.forEach((nombre, datos) -> {
            if (datos.size() >= 2) {
                String puerto = datos.get(0);
                String ip = datos.get(1).substring(1);
                callback.accept(nombre + " " + ip + " " + puerto);
            }
        });
    }
    
    public void enviarComando(String comando, String nombreServidor, Consumer<String> callback) {
        List<String> datos = servidores.get(nombreServidor);
        if (datos == null) {
            callback.accept("Servidor " + nombreServidor + " no encontrado.");
            return;
        }

        String puertoStr = datos.get(0);
        String ip = datos.get(1).substring(1);
        
        String[] partes = comando.split(" ");
        String mensaje = "";
        Control c = null;
        if(partes.length == 3) {
        	c = new Control(partes[0], partes[1], partes[2]);
        } else if(partes.length == 2) {
        	c = new Control(partes[0], partes[1], "");
        }
        
        if(tipo.equals("X")) {
        	mensaje = "X " + c.toXmlString();
        } else if(tipo.equals("J")) {
        	mensaje = "J " + c.toJsonString();
        }
        
        if(c.getComando().equals("formato")) {
        	if(c.getValor().toLowerCase().equals("json")) {
        		this.tipo = "J";
        	} else if(c.getValor().toLowerCase().equals("xml")) {
        		this.tipo = "X";
        	}
        }

        try {
            int puerto = Integer.parseInt(puertoStr);
            DatagramPacket packet = new DatagramPacket(
                mensaje.getBytes(),
                mensaje.length(),
                InetAddress.getByName(ip),
                puerto
            );

            String comandoStr = String.format(
                    "%s %s %s",
                    c.getComando(), c.getDestinatario(), c.getValor()
                );
            
            socket.send(packet);
            callback.accept("Comando enviado a " + nombreServidor + ": " + comandoStr);
        } catch (Exception e) {
            callback.accept("Error enviando comando: " + e.getMessage());
        }
    }
    
    public Map<String, List<String>> getServidores() {
        return new HashMap<String, List<String>>(servidores);
    }

    public void close() {
        socket.close();
    }
    
    private void guardarEnLog(String mensaje, String tipo) {
        String fecha = sdf.format(new Date());
        String lineaLog = "---- " + fecha + " ----\n" + mensaje + "\n\n";
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