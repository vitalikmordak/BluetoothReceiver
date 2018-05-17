package com.vitaliimordak.bluetoothreceiver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vitalii on 17.12.2017.
 */

public class SelectFilename extends Activity {
    private TextView textView;
    private EditText editText;
    private Button button;
    private String fileName;
    private ListView listView;
    private List<String> files;
    public FileListAdapter fileListAdapter = null;
    private final File ROOT = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.vitaliimordak.bluetoothreceiver/files");

    public String getFileName() {
        return fileName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_filename);

        textView = findViewById(R.id.text_enter_filename);
        listView = findViewById(R.id.files);
        editText = findViewById(R.id.edittext_name);
        button = findViewById(R.id.button_save_filename);
        fileName = editText.getText().toString().replaceAll(" ", "_").toLowerCase();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectFilename.this, Select.class);
                intent.putExtra("name", editText.getText().toString().replaceAll(" ", "_").toLowerCase());
                startActivity(intent);
            }
        });
        getFileList();
        fileListAdapter = new FileListAdapter(this, files);
        listView.setAdapter(fileListAdapter);

    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public class FileListAdapter extends ArrayAdapter<String> {
         private Activity context;
        private List<String> list;

        FileListAdapter(Activity context, List<String> objects) {
            super(context, R.layout.list_item, objects);
            this.context = context;
            this.list = objects;
        }

        @SuppressLint("ViewHolder")
        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            final ImageView share = convertView.findViewById(R.id.img_share);
            final TextView fileName =  convertView.findViewById(R.id.text_filename);
            ImageView delete = convertView.findViewById(R.id.img_delete);
            fileName.setText(list.get(position));
//            share.setImageResource(android.R.drawable.ic_menu_share);
//            delete.setImageResource(android.R.drawable.ic_menu_delete);

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(ROOT.getAbsolutePath() + "/" + fileName.getText());
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/vnd.ms-excel");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                }
            });

            fileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(ROOT.getAbsolutePath() + "/" + fileName.getText());
                    Intent intentOpenXls = new Intent(Intent.ACTION_VIEW);
                    intentOpenXls.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
                    intentOpenXls.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        startActivity(Intent.createChooser(intentOpenXls, getString(R.string.choose_xls_viewer)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), R.string.no_found_app_for_xls, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final File file = new File(ROOT.getAbsolutePath() + "/" + fileName.getText());
                    // alert message: Are you sure?
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    file.delete();
                                    list.remove(position);
                                    fileListAdapter.notifyDataSetChanged();
                                    if (!file.exists())
                                        Toast.makeText(context, getString(R.string.file)+ fileName.getText() + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(getString(R.string.alert_message_to_delete) + fileName.getText()+"?").setPositiveButton(R.string.alert_yes, dialogClickListener)
                            .setNegativeButton(R.string.alert_no, dialogClickListener).show();
                }
            });

            return convertView;
        }
    }


    private void getFileList() {
        File[] files1 = ROOT.listFiles();
        files = new ArrayList<>();
        for (File file : files1) {
            files.add(file.getName());
//            Toast.makeText(this, file.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}
