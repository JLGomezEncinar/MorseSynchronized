package org.example;

import org.example.MorseAudio;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;


/****************************************************************************************************************************************
 *   CLASE: "Morse"                                                                                                                  *
 ****************************************************************************************************************************************
 *   @author  José Luis Gómez                                                                                                                  *
 *   @version 1.0 - Versión inicial de la clase.                                                                                        *
 *   @since 27JAN26                                                                                                                 *
 ****************************************************************************************************************************************
 *   COMENTARIOS:                                                                                                                       *
 *        - Clase con dos semáforos que convierte un texto a morse.                                                       *
 ****************************************************************************************************************************************/
public class MorseSynchronized {
    // Constantes de clase
    public static int NUMERO_HILOS = 2;
    public static final String TEXTO = "Nací el 15 de enero de 1980";
    public static final String TEXTO_CONVERTIDO = TEXTO.toUpperCase().replaceAll("[ÁÀÄÂÃÅĀ]", "A")
            .replaceAll("[ÉÈËÊĒĘ]", "E")
            .replaceAll("[ÍÌÏÎĪ]", "I")
            .replaceAll("[ÓÒÖÔÕŌØ]", "O")
            .replaceAll("[ÚÙÜÛŪ]", "U");
    public static final String[] TEXTO_PALABRAS = TEXTO_CONVERTIDO.split(" ");
    public static final String ABECEDARIO_MORSE[] = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",      // A-I         (00-08)
            ".---", "-.-", ".-..", "--", "-.", "--.--", "---", ".--.", "--.-",  // J-Ñ-Q       (09-17)
            ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",    // R-Z         (18-26)
            "-----", ".----", "..---", "...--", "....-",                        // 0-4         (27-31)
            ".....", "-....", "--...", "---..", "----.",                        // 5-9         (32-36)
            ".-.-.-", "--..--", "---...", "..--..", "..--..", ".----.",         // . , : ¿ ? ' (37-42)
            "-....-", "-..-.", "-.--.-", "-.--.-", ".-..-."};                  // - / ( ) "   (45-47)

    public static final String ABECEDARIO = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ" +
            "0123456789" +
            ".,:¿?'-/()\"";


    public static void main(String[] args) {

        Buzon a_Buzon = new Buzon();

        ThreadPoolExecutor l_Executor = (ThreadPoolExecutor)
                Executors.newFixedThreadPool(NUMERO_HILOS);
        Productor l_Productor = new Productor(a_Buzon);
        Consumidor l_Consumidor = new Consumidor(a_Buzon);
        l_Executor.execute(l_Productor);
        l_Executor.execute(l_Consumidor);
        l_Executor.shutdown();
    } //main


} // Morse

class Productor implements Runnable {

    // Variables de clase
    private Buzon a_Buzon = null;


    public Productor(Buzon p_Buzon) {
        a_Buzon = p_Buzon;
    } // Productor ()


    @Override
    public void run() {

        for (int l_Contador = 0; l_Contador < MorseSynchronized.TEXTO_PALABRAS.length; l_Contador++) {

           a_Buzon.esperar();

                a_Buzon.setA_Palabra(MorseSynchronized.TEXTO_PALABRAS[l_Contador]);
                a_Buzon.notificar();
                ;





        }
    } // run
} // Productor

class Consumidor implements Runnable {

    // Variables de claee
    private Buzon a_Buzon = null;
    private String l_PalabraATraducir = "";
    private int l_ContadorLetras = 0;
    private String l_PalabraTraducida = null;
    private MorseAudio l_Reproductor = new MorseAudio();


    public Consumidor(Buzon p_Buzon) {
        a_Buzon = p_Buzon;
    } // Consumidor ()

    @Override
    public void run() {
        for (l_ContadorLetras = 0; l_ContadorLetras < MorseSynchronized.TEXTO_PALABRAS.length; l_ContadorLetras++) {

        if (l_ContadorLetras>0) {
            a_Buzon.esperar();
        }
                l_PalabraATraducir = a_Buzon.getA_Palabra();
                l_PalabraTraducida = traducirPalabraAMorse(l_PalabraATraducir);


                procesarSalida(l_PalabraATraducir, l_PalabraTraducida);


           a_Buzon.notificar();
        }
    } // run

    // Función que lee la palabra traducida y emite los pitidos.
    private void procesarSalida(String p_PalabraATraducir, String p_PalabraTraducida) {
        System.out.println(p_PalabraATraducir + " -> " + p_PalabraTraducida);
        l_Reproductor.reproducirAudioMorse(p_PalabraTraducida);
        l_Reproductor.silencio("PALABRA");
    } // procesarSalida

    // Función que recibe la palabra a traducir y la traduce a morse
    private String traducirPalabraAMorse(String p_PalabraAConvertir) {

        String l_PalabraTraducida = "";
        int l_Contador = 0;
        int l_IndiceLetra = 0;
        char l_LetraATraducir;

        for (l_Contador = 0; l_Contador < p_PalabraAConvertir.length(); l_Contador++) {
            l_LetraATraducir = p_PalabraAConvertir.charAt(l_Contador);
            l_IndiceLetra = MorseSynchronized.ABECEDARIO.indexOf(l_LetraATraducir);
            // Añadimos la letra si existe en el abecedario y si no existe mostramos un error
            if (l_IndiceLetra >= 0) {
                l_PalabraTraducida += MorseSynchronized.ABECEDARIO_MORSE[l_IndiceLetra] + " ";
            } else {
                System.out.println("No se pudo traducir la letra " + l_LetraATraducir);
            }
        }

        return l_PalabraTraducida;
    } // traducirPalabraAMorse
} // Consumidor


class Buzon {

    // Variables de clase
    private String a_Palabra = null;
    private boolean l_isTurnoProductor = true;

    // Getters y setters



    public String getA_Palabra() {
        return a_Palabra;
    }

    public void setA_Palabra(String a_Palabra) {
        this.a_Palabra = a_Palabra;
    }
    public synchronized void esperar(){
        try {
            wait();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized void notificar() {
        notifyAll();
    }


} // Buzon



