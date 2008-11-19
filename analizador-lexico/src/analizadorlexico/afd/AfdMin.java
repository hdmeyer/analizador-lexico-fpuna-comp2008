/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package analizadorlexico.afd;

import analizadorlexico.Alfabeto;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *Esta clase implementa las operaciones necesarias para pasar desde
 * un Afd a un AFD con estados mínimos.
 * @author Hugo Meyer - Leopoldo Polleti
 */
public class AfdMin {
    /*Este afd es la entrada a ser procesada por el afd minimo*/
    private AFDEquivalente afd;
    /* Vienen siendo como el PiInicial y Pi actual*/
    private ArrayList<Grupo> conjuntoInicial;
    private ArrayList<Grupo> conjuntoActual;
    /*Matriz de estados versus alftabeto*/
    private ConjuntoDeEstados [][] matrizEstAlf;
    private int vectorGrupo [];
    /*variable que mantiene el nro de grupo creado*/
    private int contGrupo =0;
    ArrayList<String> alfa;
    
    private Grupo [][] matrizMinima;
    /**
     * Constructor que utiliza un afd para setear los valores
     * iniciales
     * @param afd El afd a minimizar
     */
    public AfdMin(AFDEquivalente afd) {
        this.setAfd(afd);
        this.setConjuntoActual(new ArrayList<Grupo>());
        this.setConjuntoInicial(new ArrayList<Grupo>());
        this.vectorGrupo = new int [afd.getEstadosMarcados().size()];
    }
    
    /**
     * Esta función realiza la minimización del afd en cuestión y devuelve un afd
     * minimizado.
     * @return Retorna el afd minimizado
     */
    public AfdMin minimizacion(){
        /*Grupos iniciales que se cargan en el conjunto inicial*/
        Grupo finales = new Grupo(contGrupo);
        contGrupo++;
        Grupo noFinales = new Grupo(contGrupo);
        Grupo grupoAIntroducir;
        
        /*Obtiene los caracteres del alfabeto*/
        alfa = (ArrayList<String>) this.afd.getAfn().getAlfabeto().getCaracteres();
        
        finales.setGrupo((ArrayList<ConjuntoDeEstados>) this.afd.getEstadosFinales());
        finales.setFinales(true);
        /* Iteramos sobre los estados marcados, y si no son finales los añadimos
         *al grupo de no finales. Ademas establecemos el id de grupo 0 a los
         finales y 1 a los no finales*/
        Iterator itConjEstados = this.afd.getEstadosMarcados().iterator();
        
        while(itConjEstados.hasNext()){
            ConjuntoDeEstados aux = (ConjuntoDeEstados) itConjEstados.next();
            if(!aux.isConjFinal()){
                aux.setIdGrupo(1);
                noFinales.getGrupo().add(aux);
                this.vectorGrupo[aux.getInicio()] = 1;
            }else{
                aux.setIdGrupo(0);
                this.vectorGrupo[aux.getInicio()] = 0;
            }
        }
        if(finales.getGrupo().size()>0){
            this.conjuntoInicial.add(finales);
        }
        if(noFinales.getGrupo().size()>0){
            this.conjuntoInicial.add(noFinales);
        }
        boolean continuar =true;
        /*guarda el valor del caracter leido de la matriz*/
        String valorCaracter;
        /*SIMPLE CONTADOR PARA VERIFICAR LA CANTIDAD DE ACIERTOS DE LAS LETRAS*/
        int cont =0;
        /*Permite guardar la columna en la que se encuentra la letra buscada
         *con buscarLetra*/
        int posLetra;
        this.inicializarMatriz();
        /*Mientras no lleguemos a la condición de parada*/
        while(continuar){
            
            int tamañoConjInicial = this.conjuntoInicial.size();
            /*iteramos sobre cada grupo del conjunto inicial*/
            for(Grupo grupo : this.conjuntoInicial){
                /*si el grupo tiene mas de un elemento lo procesamos, sino
                 *simplemente lo añadimos*/
                if(grupo.getGrupo().size() > 1){
                    
                    boolean seCreoNuevoGrupo = true;
                    contGrupo++;
                    grupoAIntroducir = new Grupo(contGrupo);
                    Iterator<ConjuntoDeEstados> itElemGrupo = grupo.getGrupo().iterator();
                    
                    while(itElemGrupo.hasNext()){
                        ConjuntoDeEstados conjAux = itElemGrupo.next();
                        /*Si es un elemento ya añadido al grupo actual, lo remuevo,
                         *sino creo un nuevo grupo, dado que los que ya fueron
                         *cargados en grupo a introducir son todos los que apuntan a un mismo
                         *grupo*/
                        if(grupoAIntroducir.getGrupo().contains(conjAux)){
                            /*Luego revisamos si el grupo ya no fue cargado en el conjunto actual*/
                            itElemGrupo.remove();
                            continue;
                        }else if(grupoAIntroducir.getGrupo().size() > 0){
                            this.conjuntoActual.add(grupoAIntroducir);
                            contGrupo++;
                            grupoAIntroducir = new Grupo(contGrupo);
                        }
                        /*TOMADO EL ESTADO LO REMUEVO*/
                        itElemGrupo.remove();
                        grupoAIntroducir.getGrupo().add(conjAux);
                        /*CREO UN NUEVO ITERADOR SOBRE LOS ELEMENTOS RESTANTES EN EL GRUPO*/
                        Iterator<ConjuntoDeEstados> itNextEGrupo = grupo.getGrupo().iterator();
                        
                        /*SI YA NO AGARRAMOS MAS ESTADOS PORQUE YA ES EL ULTIMO, CARGAMOS
                         *SU GRUPO EN CONJUNTO ACTUAL*/
                        if(!itNextEGrupo.hasNext()){
                            this.conjuntoActual.add(grupoAIntroducir);
                        }
                        while(itNextEGrupo.hasNext()){
                            ConjuntoDeEstados conjAux2 = itNextEGrupo.next();
                            cont = 0;
                            for(String caracter : alfa){
                                /*verificamos si apuntan al mismo grupo ambos con la misma variable
                                 Es necesario revisar antes que las entradas de la matriz no sean
                                 null, en cuyo caso se hacen dos tipos de revisiones, si ambas son null
                                 entonces se considera del mismo grupo, si uno solo es null, son de grupos
                                 diferentes*/
                                posLetra = conseguirIdLetra(caracter);
                                boolean aComparar;
                                if(this.matrizEstAlf[conjAux.getInicio()][posLetra] == null &&
                                        this.matrizEstAlf[conjAux2.getInicio()][posLetra]==null){
                                    aComparar = true;
                                }else if(this.matrizEstAlf[conjAux.getInicio()][posLetra] == null | 
                                        this.matrizEstAlf[conjAux2.getInicio()][posLetra] ==null){
                                    aComparar=false;
                                }else{
                                    aComparar =this.matrizEstAlf[conjAux.getInicio()][posLetra].getIdGrupo() == 
                                        this.matrizEstAlf[conjAux2.getInicio()][posLetra].getIdGrupo();
                                }
                                if(aComparar){
                                    cont++;
                                }
                            }
                            /*VERIFICAMOS SI SE CUMPLIO QUE APUNTAN AL MISMOS GRUPOS CON LAS MISMAS LETRAS*/
                            if(cont == alfa.size()){
                                conjAux2.setIdGrupo(grupoAIntroducir.getIdGrupo());
                                grupoAIntroducir.getGrupo().add(conjAux2);
                                itNextEGrupo.remove();
                                itElemGrupo = grupo.getGrupo().iterator();
//                                for(String caracter : alfa){
//                                    posLetra = conseguirIdLetra(caracter);
//                                    Grupo g = this.dondeEstaElEstado(this.matrizEstAlf[conjAux2.getInicio()][posLetra]);
//                                    if(g !=null){
//                                        g.setEnlace(caracter);
//                                    }
//                                    grupoAIntroducir.getDestino().add(g);                      
//                                }                     
                            }
                        }
                        conjAux.setIdGrupo(grupoAIntroducir.getIdGrupo());
                    }
                    
                    /*
                     *Revisamos si quedo algo que no se cargo y quedo en
                     *el grupo a añadir.
                     */
                    if(grupoAIntroducir.getGrupo().size() > 0 &&
                            !this.conjuntoActual.contains(grupoAIntroducir)){
                        
                        this.conjuntoActual.add(grupoAIntroducir);
                        contGrupo++;
                        grupoAIntroducir = new Grupo(contGrupo);
                        
                    }
                }else if(grupo.getGrupo().size() > 0){
                    this.conjuntoActual.add(grupo);
                }
                
            }
            /*Si no se crearon nuevos grupos, terminamos el algoritmo*/
            if(tamañoConjInicial == this.conjuntoActual.size()){
                continuar=false;
                conjuntoInicial = conjuntoActual;
            }else if(tamañoConjInicial > this.conjuntoActual.size()){
                continuar=false;
            }else{
                conjuntoInicial = conjuntoActual;
                conjuntoActual = new ArrayList<Grupo>();
            }
            
        }
        this.construirMatrizMinima();
        return this;
    }
    
    /**
     * Metodo que parte de la matriz de adyacencias del afd y
     * construye la matriz de conjunto de estados versus letras
     * del alfabeto. Para poder utilizarla como se propone en los
     * ejercicios del cuaderno
     */
    public void inicializarMatriz(){
        int tamanoFila= this.afd.getEstadosMarcados().size();
        int tamanoCol = this.afd.getAfn().getAlfabeto().getCaracteres().size();
        this.matrizEstAlf = new ConjuntoDeEstados [tamanoFila][tamanoCol];
        String letra;
        for(int i=0; i<tamanoFila;i++){
            for(int j=0; j<tamanoFila;j++){
                letra =this.afd.getAdyacencia()[i][j];
                if (letra != null){
                   // this.conjuntoInicial
                    if(this.getAfd().getEstadosMarcados().get(j).isConjFinal()){
                        this.getAfd().getEstadosMarcados().get(j).setIdGrupo(0);
                    }else{
                        this.getAfd().getEstadosMarcados().get(j).setIdGrupo(1);
                    }
                    this.matrizEstAlf[i][this.conseguirIdLetra(letra)] = this.getAfd().getEstadosMarcados().get(j);
                }
            }
        }
    }
    /**
     * Este metodo nos permite averiguar la columna
     * a la que pertenecera un conjunto de estados.
     * Entiendanse por conjunto de estados como
     * los nodos dentro del afd
     * @param letra Recibe la letra o caracter a procesar
     * @return un entero con la posición de la letra en la matrizAlfVsConjestados
     */
    public int conseguirIdLetra(String letra){
        int pos =-1;
        for(String caracter :(ArrayList<String>) this.afd.getAfn().getAlfabeto().getCaracteres()){
            pos++;
            if(caracter.equalsIgnoreCase(letra)){
                return pos;
            }
        }
        return -1;
    }
    
    /**
     * Metodo que actualiza el vector de grupos
     * a los que pertenece un conj de estados
     */
    public void actualizarVector(){
        for(Grupo grupo : this.conjuntoInicial){
            Iterator<ConjuntoDeEstados> itElemGrupo = grupo.getGrupo().iterator();
            while(itElemGrupo.hasNext()){
                this.vectorGrupo[itElemGrupo.next().getInicio()] = grupo.getIdGrupo();
            }
        }
    }
    
    public void construirMatrizMinima(){
        this.matrizMinima = new Grupo[this.conjuntoInicial.size()][this.alfa.size()];
        int posLetra;
        this.renumerarGrupos();
        for(Grupo g : this.conjuntoInicial){
            for(String letra:alfa){
                ConjuntoDeEstados conjEst = g.getGrupo().get(0);
                posLetra = conseguirIdLetra(letra);
                ConjuntoDeEstados aux = this.matrizEstAlf[conjEst.getInicio()][posLetra];
                for(Grupo g1 : this.conjuntoInicial){
                    if(g1.getGrupo().contains(aux)){
                        matrizMinima[g.getIdGrupo()][posLetra] = g1;
                        break;
                    }
                }
            }
        }
        
    }
    public void renumerarGrupos(){
        int numeracionGrupos[] = new int[this.conjuntoInicial.size()];
        int pos =0;
        for(Grupo g : this.conjuntoInicial){
            numeracionGrupos[pos]=g.getIdGrupo();
            pos++;
        }
        int nuevoId =0;
        java.util.Arrays.sort(numeracionGrupos);
        for(int i=numeracionGrupos.length-1 ; i>-1 ; i--){
            for(Grupo g : this.conjuntoInicial){
                if(g.getIdGrupo()==numeracionGrupos[i]){
                    g.setIdGrupo(nuevoId);
                    nuevoId++;
                    break;
                }
            }
        }
        
    }
    public Grupo dondeEstaElEstado(ConjuntoDeEstados conjEstados){
        Grupo retorno=null;
        for(Grupo g:this.conjuntoInicial){
            if(g.getGrupo().contains(conjEstados)){
                retorno=g;
                break;
            }
        }
        return retorno;
    }
    
    /* GETTERS Y SETTERS DE VARIABLES*/
    public /*Este afd es la entrada a ser procesada por el afd minimo*/
    AFDEquivalente getAfd() {
        return afd;
    }
    
    public void setAfd(AFDEquivalente afd) {
        this.afd = afd;
    }

    public ArrayList<Grupo> getConjuntoInicial() {
        return conjuntoInicial;
    }

    public void setConjuntoInicial(ArrayList<Grupo> conjuntoInicial) {
        this.conjuntoInicial = conjuntoInicial;
    }

    public ArrayList<Grupo> getConjuntoActual() {
        return conjuntoActual;
    }

    public void setConjuntoActual(ArrayList<Grupo> conjuntoActual) {
        this.conjuntoActual = conjuntoActual;
    }

    public ConjuntoDeEstados[][] getMatrizEstAlf() {
        return matrizEstAlf;
    }

    public void setMatrizEstAlf(ConjuntoDeEstados[][] matrizEstAlf) {
        this.matrizEstAlf = matrizEstAlf;
    }

    public Grupo[][] getMatrizMinima() {
        return matrizMinima;
    }

    public void setMatrizMinima(Grupo[][] matrizMinima) {
        this.matrizMinima = matrizMinima;
    }
    
}
