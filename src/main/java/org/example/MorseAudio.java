package org.example;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/****************************************************************************************************************************************
 *   CLASE: src.MorseAudio                                                                                                                  *
 ****************************************************************************************************************************************
 *   PROGRAMACIÓN DE SERVICIOS Y PROCESOS 2DAM  -  Eclipse IDE for Java Developers v2024-12 (4.34.0)                                    *
 ****************************************************************************************************************************************
 *   @author  S.García                                                                                                                  *
 *   @version 1.2 - Creación del método reproducirAudioMorse() para mejorar la usabilidad de la librería.                               *
 *            1.1 - Mejora en la nitidez del sonido y adopción de la frecuencia recomendada.                                            *
 *            1.0 - Versión inicial de la clase.                                                                                        *
 *   @since   20ENE2025                                                                                                                 *
 *            16ENE2025                                                                                                                 *
 *            10ENE2025                                                                                                                 *
 ****************************************************************************************************************************************
 *   COMENTARIOS:                                                                                                                       *
 *      - Generador de tonos para uso en entrenamiento Morse.                                                                           *
 *      - Basado en AudioSystem.getSourceDataLine().                                                                                    *
 *      - Cumple con las proporciones de tiempos Morse.                                                                                 *
 ****************************************************************************************************************************************/
public class MorseAudio
{

    // Constantes asociadas al generador de tonos.
    public static float FRECUENCIA_MUESTREO = 8_000f;
    public static double VOLUMEN = 1.0d;
    public static int BITS_MUESTRA = 8;
    public static int CANALES = 1;
    public static int FRECUENCIA_BASE = 700;

    // Constantes asociadas a las reglas Morse (tiempos en milisegundos).
    public static int MS_PUNTO = 60;
    public static int MS_RAYA = MS_PUNTO * 3;
    public static int MS_SILENCIO_SIMBOLO = MS_PUNTO;
    public static int MS_SILENCIO_LETRA = MS_PUNTO * 3;
    public static int MS_SILENCIO_PALABRA = MS_PUNTO * 7;


    /*
     *   COMENTARIOS: Reproduce por los altavoces del equipo el sonido Morse correspondiente a la secuencia Morse dada.
     *   @author  S.García
     *   @param   p_PalabraMorse - EntradaSimple - Secuencia de puntos, rayas, y blancos, correspondientes a la codificación morse de una palabra.
     *   @return  void
     */
    public void reproducirAudioMorse(String p_PalabraMorse)
    {
        int l_Indice = 0;

        // Mientras queden símbolos por reproducir en la palabra Morse.
        while (l_Indice < p_PalabraMorse.length())
        {

            switch (p_PalabraMorse.charAt(l_Indice))
            {
                case '.' -> simbolo("PUNTO",true);
                case '·' -> simbolo("PUNTO",true);
                case '-' -> simbolo("RAYA",true);
                case '_' -> simbolo("RAYA",true);
                case ' ' -> silencio("LETRA");
                default  -> System.out.println("ERROR: Símbolo no reconocido.");
            }
            l_Indice++;

        }

    }   // reproducirAudioMorse()


    /*
     *   COMENTARIOS: Genera un tono por la salida estándar de sonido de duración variable en función de si estamos
     *                reproduciendo un PUNTO o una RAYA.
     *   @author  S.García
     *   @param   p_Simbolo - EntradaSimple - Indica si se ha de reproducir un PUNTO o una RAYA.
     *   @param   p_Silencio - EntradaSimple - Indica si tras reproducir el símbolo ha de realizarse la pausa indicada por las reglas Morse.
     *   @return  void
     */
    public void simbolo(String p_Simbolo, boolean p_Silencio)
    {
        switch (p_Simbolo)   // En función de si es un punto o una raya.
        {
            case "PUNTO" -> tono(FRECUENCIA_BASE, MS_PUNTO, VOLUMEN);
            case "RAYA"  -> tono(FRECUENCIA_BASE, MS_RAYA, VOLUMEN);
            default      -> System.out.println("ERROR: Símbolo no reconocido.");
        }
        if (p_Silencio) silencio ("SIMBOLO");
    }   // simbolo()


    /*
     *   COMENTARIOS: Pausa el hilo el tiempo especificado por las reglas morse tras un SIMBOLO, LETRA o una PALABRA.
     *                reproduciendo un PUNTO o una RAYA.
     *   @author  S.García
     *   @param   p_Entidad - EntradaSimple - Indica el tipo de silencio que se desea realizar.
     *   @return  void
     */
    public void silencio (String p_Entidad)
    {
        try
        {
            switch (p_Entidad)   // En función del tipo de entidad que acaba.
            {
                case "SIMBOLO" -> Thread.sleep(MS_SILENCIO_SIMBOLO);
                case "LETRA"   -> Thread.sleep(MS_SILENCIO_LETRA);
                case "PALABRA" ->  Thread.sleep(MS_SILENCIO_PALABRA);
                default -> System.out.println("ERROR: Entidad no reconocida.");
            }
        }
        catch (InterruptedException p_Excepcion)
        {
            System.out.println("ERROR: Ha fallado un sleep() ("+
                    p_Excepcion.getLocalizedMessage() + ").");
        }
    }   // silencio()


    /*
     *   COMENTARIOS: Generador de tonos sinusoidales para corriente de audio.
     *   @author  S.García
     *   @param   p_Hz - EntradaSimple - Frecuencia del tono a generar.
     *   @param   p_Mseg - EntradaSimple - Duración del tono en milisegundos.
     *   @param   p_Volumen - EntradaSimple - Volumen relativo del tono a generar.
     *   @return  void
     */
    public static void tono(int p_Hz, int p_Msegs, double p_Volumen)
    {
        int l_Contador = 0;
        double l_Angulo = 0.0d;
        byte[] l_Frame = new byte[1];
        SourceDataLine l_Corriente = null;
        AudioFormat l_Formato = new AudioFormat( FRECUENCIA_MUESTREO,  // sampleRate -> Frames por segundo.
                BITS_MUESTRA,         // sampleSizeInBits -> Tamaño del frame.
                CANALES,              // channels -> 1 mono, 2 stereo, etc.
                true,                 // signed -> Si la información en el byte son 7 u 8 bits.
                false );              // bigEndian -> Si los bytes están en dicho formato.

        // Creamos e inicializamos la corriente audio-out por defecto del mezclador.
        try   {
            l_Corriente = AudioSystem.getSourceDataLine(l_Formato);   // Obtener la corriente que el mezclador está usando para audio-i n.
            l_Corriente.open(l_Formato);   // Abrir la corriente para "frames" de sonido del formato especificado.
            l_Corriente.start();   // Empezar a procesar los frames que se vayan generando.
            l_Corriente.flush();   // Como flush() pero para corrientes.
        }
        catch (LineUnavailableException p_Excepcion)
        {
            System.out.println("ERROR: Ha fallado la inicialización de la corriente audio-out ("+
                    p_Excepcion.getLocalizedMessage() + ").");
        }

        // Generar la información del frame y escribirlo en la corriente audio-out.
        for (l_Contador=0; l_Contador < p_Msegs*8; l_Contador++)
        {
            l_Angulo = l_Contador / (FRECUENCIA_MUESTREO / p_Hz) * 2.0d * Math.PI;
            l_Frame[0] = (byte)(Math.sin(l_Angulo) * 127.0d * p_Volumen);
            l_Corriente.write(l_Frame,0,1);  // Escribir en el canal el frame con offset 0 y longitud 1.
        }

        l_Corriente.drain();   // Como flush() pero para corrientes.
        l_Corriente.stop();    // Parar el procesamiento de la corriente para dar tiempo suficiente a que se reproduzca el frame completo antes de destruir la corriente.
        l_Corriente.close();   // Destruir la corriente liberando sus recursos.
    }   // tono()

}    //src.MorseAudio
