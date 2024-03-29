/*
 * Main.java
 *
 * Created on 1 de noviembre de 2008, 12:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package analizadorlexico;

import analizadorlexico.afd.AFDEquivalente;
import analizadorlexico.afd.AfdMin;

/**
 *
 * @author Hugo Daniel Meyer - Leopoldo Poletti
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here                
        Alfabeto alf = new Alfabeto();
        alf.cargarAlfabeto("a,b,c,E");
        alf.setHayVacio(true);                
        
        Afn a = new Afn("(a|b)*$", alf);
        a = a.generar();
        AFDEquivalente afd = new AFDEquivalente(a);
        afd.construirTransiciones();
        afd.validarCadena("aabba");
        AfdMin afdmin = new AfdMin(afd);
        afdmin.inicializarMatriz();
        String respondeAFD =afd.imprimirMatriz(afdmin);
        a.cargaMatriz();
        String responde = a.imprimirMatriz();
        afdmin = afdmin.minimizacion();
        String respondeAFDmin = afdmin.imprimirMatriz();
        a.setCadenaEntrada("abb");
        boolean validado = a.validacion();
        boolean validadoAfdMin = afdmin.validarCadena("aabba");
        System.out.println(a.imprimir());
        System.out.println(afd.imprimir());
    }
    
}
