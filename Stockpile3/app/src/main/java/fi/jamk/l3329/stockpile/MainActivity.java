package fi.jamk.l3329.stockpile;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ShoplistDialogFragment.ShopListDialogListener,
        ItemAdapter.OnItemCheckListener,
        DeleteItemDialog.LongClickItemListener
{

    private static final String TAG = "MainActivity";
    private static File DB_FILE_PATH;

    int RC_SIGN_IN = 2;

    //    private GoogleSignInClient mGoogleSignInClient;
    private DriveId mDriveId;
    private DriveClient mDriveClient;
    private DriveContents mDriveContents;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private List<Item> items;
    private DatabaseOpenHelper db;

    private GoogleSignInClient mGoogleSignInClient;
    private DriveResourceClient mDriveResourceClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //inits floating action button
        initFab();

        //get database instance
        db = new DatabaseOpenHelper(this);
        DB_FILE_PATH = this.getDatabasePath("database.db");

        //get data with using own made queryData method
        items = db.getAllItems();


        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setAdapter(new ItemAdapter(items, this, this, getFragmentManager()));
        registerForContextMenu(mRecyclerView);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        //Initialize Google Drive API Client
        Log.i(TAG, "Start sign in");
        mGoogleSignInClient = buildGoogleSignInClient();
        signIn();


    }



    public void initFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShoplistDialogFragment dialogFragment = new ShoplistDialogFragment();
                dialogFragment.show(getFragmentManager(), "newShopList");

            }
        });
    }

    public void findFileToDownload(String nameOfFile, final boolean write) {
        //at first you have to find the file
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, nameOfFile))
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/x-sqlite3"))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .build();

        //launch query asynchronously
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);


        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {

                                if (metadataBuffer.getCount() != 0 && metadataBuffer.get(0).isTrashed()) {  //there has to be at least one field and cannot be trashed
                                    Log.e(TAG, "File does not exist.");
                                    Toast.makeText(getApplicationContext(), "File does not exist.", Toast.LENGTH_SHORT).show();

                                }else{


                                    if (metadataBuffer.getCount() == 0 && write) {  //there is no such file in drive & if we want to write we will have to create new file
                                        saveFileToDrive();
                                    } else if (metadataBuffer.getCount() != 0) {    //there was something found
                                        //get its driveId
                                        mDriveId = metadataBuffer.get(0).getDriveId();
                                        //handle it as file
                                        openFile(mDriveId.asDriveFile(), write);
                                    } else {    //we do not want to create(write flag) file therefore we want to read(write = false), but there was found no file
                                        Log.e(TAG, "File does not exist.");
                                        Toast.makeText(getApplicationContext(), "File does not exist.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                metadataBuffer.release();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failure in findFileToDownload method: " + e.getMessage());
                    }
                });
    }


    public void openFile(DriveFile driveFile, final boolean write) {

        //do we want to write to or read the file?
        int mode = write ? DriveFile.MODE_WRITE_ONLY : DriveFile.MODE_READ_ONLY;

        //open file asynchronously
        Task<DriveContents> openFileTask =
                mDriveResourceClient.openFile(driveFile, mode);

        openFileTask
                .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                        //retrieve result
                        mDriveContents = task.getResult();

                        //if we want to write
                        if (write){
                            updateFile(mDriveContents); //since we are in this method it means that we have already found the file and that we want to update it
                        }
                        else {
                            syncToDevice(mDriveContents);   //if we found the file but do not want to write to it that means that we are going to download it from drive
                        }

                        Task<Void> commitTask =
                                mDriveResourceClient.commitContents(mDriveContents, null);
//                        Task<Void> discardTask = mDriveResourceClient.discardContents(mDriveContents);
                        return commitTask;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failure in openFile method: " + e.getMessage());
                    }
                });
    }

    private void updateFile(DriveContents driveContents){

        //get the outputstream
        OutputStream outputStream = driveContents.getOutputStream();

        //write to the stream
        try {
            File file = DB_FILE_PATH;
            InputStream is = new FileInputStream(file);
            byte[] buf = new byte[4096];
            int c;
            while ((c = is.read(buf, 0, buf.length)) > 0) {
                outputStream.write(buf, 0, c);
                outputStream.flush();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        MetadataChangeSet changeSet =new MetadataChangeSet.Builder()
                .setMimeType("application/x-sqlite3")
                .setTitle(db.getDatabaseName())
                .build();

        //and commit the changes to the file with corresponding metadata
        mDriveResourceClient.commitContents(driveContents,changeSet);
        Toast.makeText(getApplicationContext(),"File successfully synced",Toast.LENGTH_SHORT).show();

    }

    /**
     * Build a Google SignIn client.
     */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            updateViewWithGoogleSignInAccountTask(task);
        }
    }

    private void syncToDevice(DriveContents contents) {
        try {
            File file = DB_FILE_PATH;
            db.close();

            //we are going to read from drive file
            InputStream inStream = contents.getInputStream();
            InputStreamReader inStreamReader = new InputStreamReader(inStream);

            //and write to local file
            OutputStream outStream = new FileOutputStream(file);


            byte[] buf = new byte[4096];
            int c;
            while ((c = inStream.read(buf, 0, buf.length)) > 0) {
                outStream.write(buf, 0, c);
                outStream.flush();
            }

            //get database instance
            db = new DatabaseOpenHelper(this);
            //get data with using own made queryData method
            items = db.getAllItems();
            //update view
            mRecyclerView.setAdapter(new ItemAdapter(items, this, this, getFragmentManager()));

        } catch (Exception e) {
            Log.e(TAG, "Failure in openFile method I/O operation: " + e.getMessage());
        }
    }


    private void updateViewWithGoogleSignInAccountTask(Task<GoogleSignInAccount> task) {
        Log.i(TAG, "Update view with sign in account task");
        task.addOnSuccessListener(
                new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.i(TAG, "Sign in success");
                        // Build a drive client.
                        mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
                        // Build a drive resource client.
                        mDriveResourceClient =
                                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Sign in failed", e);
                            }
                        });
    }

    private void saveFileToDrive(/*final DriveContents contents*/) {

        Log.i(TAG, "Creating new mDriveContents.");

        final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {

                        DriveFolder parent = rootFolderTask.getResult();
                        DriveContents driveContents = createContentsTask.getResult();


                        Log.i(TAG, "New mDriveContents created.");
                        // Get an output stream for the mDriveContents.
                        OutputStream outputStream = driveContents.getOutputStream();

                        //write to outputstream
                        try {
                            File file = DB_FILE_PATH;
                            InputStream is = new FileInputStream(file);
                            byte[] buf = new byte[4096];
                            int c;
                            while ((c = is.read(buf, 0, buf.length)) > 0) {
                                outputStream.write(buf, 0, c);
                                outputStream.flush();
                            }

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }


                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        MetadataChangeSet changeSet =new MetadataChangeSet.Builder()
                                        .setMimeType("application/x-sqlite3")
                                        .setTitle(db.getDatabaseName())
                                        .build();

                        //create new file with data and metadata
                        return mDriveResourceClient.createFile(parent, changeSet, driveContents);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                refreshRecyclerView();
                                Toast.makeText(getApplicationContext(),"File uploaded successfully!",Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Unable to create file",Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to create file", e);
                    }
                });

    }

    public void refreshRecyclerView(){
        db = new DatabaseOpenHelper(getApplicationContext());
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setAdapter(new ItemAdapter(items, this, this, getFragmentManager()));
    }

    /*======================== methods to inflate & work with menu =============================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.download) {
            findFileToDownload(db.getDatabaseName(), false);    //button to sync from drive to device
            return true;
        } else if (id == R.id.upload) {
            findFileToDownload(db.getDatabaseName(), true);     //button to sync from device to drive
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /*======================== methods to inflate & work with menu =============================*/

    /*======================= listeners for ShoplistDialogFragment==========================*/
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String name, float price, float amount, String currency, String unit) {
        Item item = new Item.ItemBuilder()
                .name(name)
                .price(price)
                .amount(amount)
                .currency(currency)
                .units(unit)
                .build();

        items.add(item);
        db.addItem(item);

        mRecyclerView.setAdapter(new ItemAdapter(items, this, this, getFragmentManager()));
        Snackbar.make(findViewById(android.R.id.content), "Successfully created " + name, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Snackbar.make(findViewById(android.R.id.content), "Canceled", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }
    /*======================= listeners for ShoplistDialogFragment==========================*/

    /*======================= listener for ItemAdapter==========================*/
    @Override
    public void onItemCheck(int position, boolean isChecked) {
        items.get(position).setBought(isChecked);
        db.updateItem(items.get(position));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment, int position) {
        db.deleteItem(items.remove(position));
        mRecyclerView.setAdapter(new ItemAdapter(items, this, this, getFragmentManager()));
    }
    /*======================= listener for ItemAdapter==========================*/
}
