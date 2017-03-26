package co.edu.udea.compumovil.gr01_20171.proyectoescuela.Vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import co.edu.udea.compumovil.gr01_20171.proyectoescuela.Modelo.OperacionesBaseDeDatos;
import co.edu.udea.compumovil.gr01_20171.proyectoescuela.Modelo.POJO.Categoria;
import co.edu.udea.compumovil.gr01_20171.proyectoescuela.Modelo.POJO.Estudiante;
import co.edu.udea.compumovil.gr01_20171.proyectoescuela.Modelo.POJO.Grupo;
import co.edu.udea.compumovil.gr01_20171.proyectoescuela.Modelo.POJO.Materia;
import co.edu.udea.compumovil.gr01_20171.proyectoescuela.Modelo.POJO.Subcategoria;
import co.edu.udea.compumovil.gr01_20171.proyectoescuela.R;

public class SeguimientoCognitivo extends Activity {

    private ArrayList<Estudiante> estudiantes;
    private Grupo grupo;
    private int tipoVista;
    private ArrayList<Categoria> categorias;
    private ArrayList<Subcategoria> subcategorias;
    private int idEstudiante;
    private Intent intent;
    private EstadisticaCognitiva estadistica;
    private Estudiante estudiante;


    private OperacionesBaseDeDatos manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_cognitivo);
        manager = OperacionesBaseDeDatos.obtenerInstancia(getApplicationContext());
        estadistica = new EstadisticaCognitiva(manager);
        grupo = (Grupo) getIntent().getSerializableExtra("GRUPO");
        tipoVista = (int) getIntent().getSerializableExtra("tipoVista");
        //El 1 represente el valor de tipo cognitivo
        categorias = manager.obtenerCategorias(1);

    }

    /**
     * Metodo encargado de crear la grilla para mostrar los estudiantes de acuerdo a su ubicacion
     */
    private void crearGridView()
    {
        estudiantes = manager.obtenerEstudiantesDB(grupo);

        estudiantes = completarEstudiantes(estudiantes,grupo.getColumnas()*grupo.getFilas());

        EstudianteAdapter adapter = new EstudianteAdapter(this, estudiantes);
        GridView gridEstudiante = (GridView) findViewById(R.id.grid_view_ubicacion);
        gridEstudiante.setAdapter(adapter);
        gridEstudiante.setNumColumns(grupo.getFilas());
        gridEstudiante.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                estudiante = estudiantes.get(position);
                idEstudiante = estudiante.getIdentificacion();
                estadistica.setIdEstudiante(idEstudiante);
                if(tipoVista == 1 && idEstudiante != 0){

                    intent = new Intent(SeguimientoCognitivo.this, SegCogEstudiante.class);
                    intent.putExtra("id",estudiantes.get(position).getIdentificacion());
                    startActivity(intent);

                }else if(tipoVista == 2 && idEstudiante != 0){
                    AlertDialog dialog = listarOpcionesMatGral();
                    dialog.show();
                 }

            }
        });
    }


    /**
     * Metodo para listar las materias 
     * @return
     */
    public AlertDialog listarOpcionesMatGral(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<Materia> materias =  manager.obtenerMaterias();
        final CharSequence[] items = new CharSequence[materias.size()];

            for (int i=0; i<items.length;i++){
                items[i] = materias.get(i).getNombre();
            }

        builder.setTitle("Seleccione una opción").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            mostrarEstadistica(materias, which);

            }
        });

        return builder.create();

    }

    /**
     * Muestra las estadistica
     * @param materias
     * @param which
     */
    public void mostrarEstadistica(ArrayList materias,int which){

        estadistica.setMateria((Materia)materias.get(which));
        intent = new Intent(this, EstadisticaModel.class);
        intent.putStringArrayListExtra("valX", estadistica.listarCategorias(categorias));
        intent.putExtra("valSi",estadistica.obtenerValSiCategorias(categorias, idEstudiante));
        intent.putExtra("valNo", estadistica.obtenerValNoCategorias(categorias, idEstudiante));
        intent.putExtra("idEstudiante", idEstudiante);
        intent.putExtra("abrirBarra", true);
        intent.putExtra("tipoEstadistica", 1);
        intent.putExtra("materia", estadistica.getMateria());
        intent.putExtra("titulo","General categorías "+estudiante.getNombres());

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        estudiantes = manager.obtenerEstudiantesDB(grupo);

        crearGridView();
        super.onResume();
    }

    private ArrayList<Estudiante> completarEstudiantes(ArrayList<Estudiante> estudiantes, int n)
    {
        ArrayList<Estudiante> estudiantesFull = new ArrayList<Estudiante>();
        int contador = estudiantes.size();
        int filaAnt = 0;
        int j;
        for (int i = 0 ; i < contador ; i++)
        {

            int filaAct = estudiantes.get(i).getPosFila();
            while (filaAnt+1 != filaAct)
            {
                Estudiante e = new Estudiante(0,"","","",0,"",filaAnt+1,0);
                estudiantesFull.add(e);
                filaAnt++;
            }
            estudiantesFull.add(estudiantes.get(i));
            filaAnt = filaAct;
        }
        contador =estudiantesFull.size();
        if( contador < n)
        {
            int valorFila = estudiantesFull.get(contador-1).getPosFila() + 1;

            for(int i = contador ; i < n; i++)
            {
                Estudiante e = new Estudiante(0,"","","",0,"",valorFila,0);
                estudiantesFull.add(e);
                valorFila++;
            }
        }

        return estudiantesFull;
    }




}
