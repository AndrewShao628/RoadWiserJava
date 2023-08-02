package com.example.roadwiserjava;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LoadActivity extends ListActivity {
    private String m_videoRootPath = null;
    private List<String> m_fileItems = null;

    private static final String TAG = "Sigwise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler((RoadWiserApp) getApplication()));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_load);

        m_videoRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.savefolder);

        populateList();
        registerForContextMenu(this.getListView());

        SigwiseLogger.i(TAG, "LoadActivity onCreate");

    }

    void populateList( ) {
        m_fileItems = new ArrayList<String>();

        //m_fileItems = null;





        File f = new File(m_videoRootPath);
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".mp4")) {
                    return true;
                }
                return false;
            }
        });
        ArrayList<File> sortedFiles = new ArrayList<File>(Arrays.asList(files) );

        Collections.sort(sortedFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        for (int i=0; i<sortedFiles.size(); ++i) {
            File file = sortedFiles.get(i);
            m_fileItems.add(file.getName());
        }

        ArrayAdapter<String> fileListAdapter = new ArrayAdapter<String>(this, R.layout.file_list_row, m_fileItems);
        setListAdapter(fileListAdapter);


        //throw new RuntimeException("This is a crash");

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filePath = m_videoRootPath + File.separator + m_fileItems.get(position);

        Intent intent = new Intent(this, PlaybackActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("SELECTED_FILE", filePath);
        startActivity(intent);
        //finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SigwiseLogger.i(TAG, "LoadActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        goToMainActivity();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (  v.getId() == getListView().getId() ) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            String fileName = m_fileItems.get(info.position);
            menu.setHeaderTitle(fileName);
            String[] menuItems = getResources().getStringArray(R.array.FilePopupMenu);
            for (int i=0; i<menuItems.length; ++i) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String fileName = m_fileItems.get(info.position);

        int idx = item.getItemId();
        switch ( idx ) {
            case 0: {
                delFile(fileName);
                m_fileItems.remove(info.position);
                ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
                break;
            }
//            case 1: {
//                SigwiseLogger.i(TAG, "show detail for file " + fileName);
//                break;
//            }
        }

        return true;
    }

    // delete video file and its associated location file
    private void delFile( String videoFileName ) {

        File vidFile = new File( this.m_videoRootPath + File.separator + videoFileName);
        String baseName;
        if ( -1 == videoFileName.lastIndexOf('.'))
            baseName = videoFileName;
        else
            baseName = videoFileName.substring( 0, videoFileName.lastIndexOf('.') );
        File locFile = new File( vidFile.getParent() + File.separator + baseName + ".loc" );

        if ( vidFile.delete() )
            SigwiseLogger.i(TAG, "File deleted: " + vidFile.getAbsolutePath());
        if ( locFile.delete() )
            SigwiseLogger.i(TAG, "File deleted: " + locFile.getAbsolutePath());
    }
}
