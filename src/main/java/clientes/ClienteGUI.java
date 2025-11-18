package clientes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClienteGUI extends JFrame {
    private static final long serialVersionUID = 1L;
	private JTextArea areaMensajes;
    private JTextField campoComando;
    private Cliente cliente;

    public ClienteGUI() {
        super("Cliente UDP");

        cliente = new Cliente();

        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaMensajes);

        campoComando = new JTextField();
        JButton botonEnviar = new JButton("Enviar");

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(campoComando, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        ActionListener enviarAccion = e -> {
            String comando = campoComando.getText().trim();
            if (!comando.isEmpty()) {
                procesarComando(comando);
                campoComando.setText("");
            }
        };

        botonEnviar.addActionListener(enviarAccion);
        campoComando.addActionListener(enviarAccion);

        new Thread(() -> cliente.escucharConCallback(this::mostrarMensaje)).start();
    }

    private void procesarComando(String comando) {
        String[] partes = comando.split(" ");

        if (partes.length == 0) return;

        switch (partes[0].toLowerCase()) {
            case "mostrarservidores":
            	mostrarMensaje("[Comando] " + comando);
                cliente.mostrarServidoresConCallback(this::mostrarMensaje);
                break;

            case "cambiarfrecuencia":
                if (partes.length < 3) {
                    mostrarMensaje("Uso: cambiarFrecuencia <nombreServidor> <milisegundos>");
                } else {
                	mostrarMensaje("[Comando] " + comando);
                    String nombreServidor = partes[1];
                    String mensaje = comando;
                    cliente.enviarComando(mensaje, nombreServidor, this::mostrarMensaje);
                }
                break;

            case "stop":
                if (partes.length < 2) {
                    mostrarMensaje("Uso: stop <nombreServidor>");
                } else {
                	mostrarMensaje("[Comando] " + comando);
                    String nombreServidor = partes[1];
                    String mensaje = comando;
                    cliente.enviarComando(mensaje, nombreServidor, this::mostrarMensaje);
                }
                break;

            case "start":
                if (partes.length < 2) {
                    mostrarMensaje("Uso: start <nombreServidor>");
                } else {
                	mostrarMensaje("[Comando] " + comando);
                    String nombreServidor = partes[1];
                    String mensaje = comando;
                    cliente.enviarComando(mensaje, nombreServidor, this::mostrarMensaje);
                }
                break;
                
            case "stopall":
            	mostrarMensaje("[Comando] " + comando);
                cliente.getServidores().forEach((nombre, datos) -> {
                    cliente.enviarComando("stop " + nombre, nombre, this::mostrarMensaje);
                });
                mostrarMensaje("Se ha enviado stop a todos los servidores.");
                break;

            case "startall":
            	mostrarMensaje("[Comando] " + comando);
                cliente.getServidores().forEach((nombre, datos) -> {
                    cliente.enviarComando("start " + nombre, nombre, this::mostrarMensaje);
                });
                mostrarMensaje("Se ha enviado start a todos los servidores.");
                break;
            
            case "formato":
                if (partes.length < 2) {
                    mostrarMensaje("Uso: formato <nombreServidor> <XML/JSON>");
                } else {
                    String argumentoFormato = partes[2];
                    String nombreServidor = partes[1];
                    mostrarMensaje("[Comando] " + comando);
                    cliente.enviarComando("formato " + nombreServidor + " " + argumentoFormato, nombreServidor, this::mostrarMensaje);
                    mostrarMensaje("Se ha cambiado el formato en los servidores.");
                }
                break;
                
            case "formatoall":
                if (partes.length < 2) {
                    mostrarMensaje("Uso: formato <XML/JSON>");
                } else {
                    String argumentoFormato = partes[1];
                    mostrarMensaje("[Comando] " + comando);
                    cliente.getServidores().forEach((nombre, datos) -> {
                        cliente.enviarComando("formato " + nombre + " " + argumentoFormato, nombre, this::mostrarMensaje);
                    });
                    mostrarMensaje("Se ha cambiado el formato en los servidores.");
                }
                break;

            case "clear":
                areaMensajes.setText("");
                break;
                
            case "salir":
                mostrarMensaje("Cerrando cliente...");
                cliente.close();
                System.exit(0);
                break;

            case "help":
            	mostrarMensaje("[Comando] " + comando);
                mostrarMensaje("Comandos disponibles:");
                mostrarMensaje("help -> muestra el panel de ayuda");
                mostrarMensaje("mostrarServidores -> muestra los servidores disponibles");
                mostrarMensaje("cambiarFrecuencia <nombreServidor> <frecuencia (ms)> -> cambia la frecuencia de entrega");
                mostrarMensaje("stop <nombreServidor> -> detiene temporalmente el servidor");
                mostrarMensaje("start <nombreServidor> -> reanuda el servidor detenido");
                mostrarMensaje("stopAll -> detiene temporalmente todos los servidores");
                mostrarMensaje("startAll -> reanuda todos los servidores detenidos");
                mostrarMensaje("formato <nombreServidor> <XML/JSON> -> cambia el formato de envio del servidor");
                mostrarMensaje("formatoAll <XML/JSON> -> cambia el formato de envio de los servidores");
                mostrarMensaje("clear -> limpia la pantalla");
                mostrarMensaje("salir -> cierra el programa");
                break;

            default:
                mostrarMensaje("Comando no reconocido (escriba help): " + comando);
                break;
        }
    }

    private void mostrarMensaje(String texto) {
        SwingUtilities.invokeLater(() -> {
            areaMensajes.append(texto + "\n");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteGUI::new);
    }
}
