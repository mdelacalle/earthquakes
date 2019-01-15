package com.brownietech.earthquakes3d;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.G3MContext;
import org.glob3.mobile.generated.GEO2DLineStringGeometry;
import org.glob3.mobile.generated.GEO2DMultiLineStringGeometry;
import org.glob3.mobile.generated.GEO2DMultiPolygonGeometry;
import org.glob3.mobile.generated.GEO2DPointGeometry;
import org.glob3.mobile.generated.GEO2DPolygonGeometry;
import org.glob3.mobile.generated.GEO3DPointGeometry;
import org.glob3.mobile.generated.GEO3DPolygonGeometry;
import org.glob3.mobile.generated.GEOMarkSymbol;
import org.glob3.mobile.generated.GEORenderer;
import org.glob3.mobile.generated.GEOSymbol;
import org.glob3.mobile.generated.GEOSymbolizer;
import org.glob3.mobile.generated.GTask;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.IThreadUtils;
import org.glob3.mobile.generated.JSONObject;
import org.glob3.mobile.generated.LayerSet;
import org.glob3.mobile.generated.Mark;
import org.glob3.mobile.generated.Sector;
import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.URL;
import org.glob3.mobile.generated.URLTemplateLayer;
import org.glob3.mobile.specific.G3MBuilder_Android;
import org.glob3.mobile.specific.G3MWidget_Android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends Activity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mPlanetTitles;
    private ListView mDrawerList;
    private GEORenderer mVectorLayerRenderer;
    private G3MWidget_Android _g3MWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dexter.withActivity(MainActivity.this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            startGlob3();
                        } else {
                            Toast.makeText(MainActivity.this, getText(R.string.ask_for_location), Toast.LENGTH_LONG).show();
                            startGlob3();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startGlob3() {

        mPlanetTitles = getResources().getStringArray(R.array.level_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mPlanetTitles);
        mDrawerList.setAdapter(adapter);
        // Set the list's click listener
        DrawerItemClickListener listener = new DrawerItemClickListener();

        mDrawerList.setOnItemClickListener(listener);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                getActionBar().setTitle("Earthquakes 3D");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("Menu");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.menu_24px);

        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.g3m);
        final G3MBuilder_Android builder = new G3MBuilder_Android(this);

        mVectorLayerRenderer = builder.createGEORenderer(Symbolizer,true,true,true,false);
        mVectorLayerRenderer.loadJSON(new URL("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&minmagnitude=4"));
       // builder.addRenderer(vectorLayerRenderer);


        LayerSet layerSet = new LayerSet();

        final URLTemplateLayer baseLayer = URLTemplateLayer.newMercator("https://[1234].aerial.maps.cit.api.here.com/maptile/2.1/maptile/newest/satellite.day/{level}/{x}/{y}/256/png8?app_id=DemoAppId01082013GAL&app_code=AJKnXv84fjrb0KIHawS0Tg"
                , Sector.fullSphere(),false,2,18, TimeInterval.fromDays(30));

        baseLayer.setEnable(true);
        layerSet.addLayer(baseLayer);

        builder.setAtmosphere(true);

        builder.getPlanetRendererBuilder().setLayerSet(layerSet);

        _g3MWidget = builder.createWidget();

        cl.addView(_g3MWidget);

    }

    public class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int i, final long position) {



                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                    mDrawerList.setItemChecked((int)position, true);

                    Calendar cal = new GregorianCalendar();
                     Date now = cal.getTime();
//                    cal.add(Calendar.DAY_OF_MONTH, -7);
//                    Date sevenDaysAgo = cal.getTime();


                    switch ((int)position) {
                        case 0:
                            loadLayer(formatter.format(now),formatter.format(now),"0");
                            break;
                        case 1:
                            Log.e("***" , "1");
                            break;
                        case 2:
                            Log.e("***" , "2");
                            break;
                        case 3:
                            Log.e("***" , "3");
                            break;
                        case 4:
                            Log.e("***" , "4");
                            break;
                        case 5:
                            Log.e("***" , "5");
                            break;

                    }



//
//                    loadLayer();

                    mDrawerLayout.closeDrawers();
        }
    }

    private void loadLayer(final String from,final String to, final String richter)
    {
        IThreadUtils tu = _g3MWidget.getG3MWidget().getG3MContext().getThreadUtils();
        tu.invokeInRendererThread(new GTask() {
            @Override
            public void run(G3MContext context) {

                String f ="&starttime="+from;
                String t ="&endtime="+to;
                String r  = "&minmagnitude="+richter;
                String parameters = "";

                if(from.equals(to)&richter.equals("0")){
                    parameters = f;
                }

                mVectorLayerRenderer.getMarksRenderer().removeAllMarks();
                String path = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" + parameters;
                Log.e("***", "PATH--->"+path);
                mVectorLayerRenderer.loadJSON(new URL(path));
            }
        },true);
    }

    public GEOSymbolizer Symbolizer = new GEOSymbolizer() {

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO2DPointGeometry geometry) {

            final ArrayList<GEOSymbol> result = new ArrayList<GEOSymbol>();
            final JSONObject properties = geometry.getFeature().getProperties();

            Log.e("***", "earthquake!");


            final Mark mark = new Mark(

                    new URL("file:///ea_icon.png", false),  //
                    //new URL(Constants._iconsPath+"/"+_icon, false),  //
                    // new Geodetic3D(geometry.getPosition(), 40),
                    new Geodetic3D(geometry.getPosition(), 0),
                    AltitudeMode.RELATIVE_TO_GROUND,
                    0,
                    null,
                    false,
                    null,
                    true
            );
//


            result.add(new GEOMarkSymbol(mark));
            return result;
        }

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO3DPointGeometry geometry) {
            final ArrayList<GEOSymbol> result = new ArrayList<GEOSymbol>();
            final JSONObject properties = geometry.getFeature().getProperties();


//            final String busStop = properties.getAsString("CODIGOEMPR", "");
//            final String denominacion = properties.getAsString("DENOMINACI", "");

            //  BusStopTouchListener vmtl = new BusStopTouchListener(busStop,denominacion,MainActivity.this.getSupportFragmentManager(),MainActivity.this);

            final Mark mark = new Mark(

                    new URL("file:///ea_icon.png", false),  //
                    //new URL(Constants._iconsPath+"/"+_icon, false),  //
                    // new Geodetic3D(geometry.getPosition(), 40),
                    geometry.getPosition(),
                    AltitudeMode.RELATIVE_TO_GROUND,
                    0,
                    null,
                    false,
                    null,
                    true
            );
//


            result.add(new GEOMarkSymbol(mark));
            return result;
        }

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO2DLineStringGeometry geometry) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO2DMultiLineStringGeometry geometry) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO2DPolygonGeometry geometry) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO3DPolygonGeometry geometry) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ArrayList<GEOSymbol> createSymbols(GEO2DMultiPolygonGeometry geometry) {
            // TODO Auto-generated method stub
            return null;
        }

    };


}
