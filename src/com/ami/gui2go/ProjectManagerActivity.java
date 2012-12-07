package com.ami.gui2go;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.ami.gui2go.models.ActivityInfo;
import com.ami.gui2go.models.ProjectInfo;
import com.ami.gui2go.utils.FileHelper;
import com.ami.gui2go.utils.ProjectXMLParser;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ProjectManagerActivity extends Activity {
    private String[] projectNames;

    ListView projectsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_manager);

        projectNames = FindAvailableProjectNames();

        if (projectNames != null) {
            Arrays.sort(projectNames);
            projectsList = (ListView) findViewById(R.id.projectsList);
            projectsList.setAdapter(new ProjectListAdapter(projectNames));
            projectsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            projectsList.setOnItemClickListener(new ListItemClickListener());
        }
    }

    private class ListItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
            hideAllActionButtons();
            LinearLayout item = (LinearLayout) v;
            for (int i = 0; i < item.getChildCount(); i++) {
                View temp = item.getChildAt(i);
                if (temp instanceof ImageView) {
                    temp.setVisibility(View.VISIBLE);
                }
            }
            ((ListView) parent).setItemChecked(pos, true);
            String projectName = projectsList.getItemAtPosition(pos).toString();
            ProjectInfo projectInfo = findProjectInfoByName(projectName);
            showDetails(projectInfo);
        }
    }

    private class ProjectListAdapter extends ArrayAdapter<String> {

        public ProjectListAdapter(String[] projectNames) {
            super(ProjectManagerActivity.this, R.layout.project_list_item_row,
                            R.id.projectName, projectNames);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeleteListener mDeleteListener = new DeleteListener();
            RenameListener mRenameListener = new RenameListener();
            CloneListener mCloneListener = new CloneListener();
            ChangeSDKListener mChangeSDKListener = new ChangeSDKListener();
            EditListener mEditListener = new EditListener();

            View row = super.getView(position, convertView, parent);

            ImageView editBtn = (ImageView) row
                            .findViewById(R.id.btn_edit_proj);
            editBtn.setOnClickListener(mEditListener);
            editBtn.setTag(position);

            ImageView deleteBtn = (ImageView) row
                            .findViewById(R.id.btn_delete_proj);
            deleteBtn.setOnClickListener(mDeleteListener);
            deleteBtn.setTag(position);

            ImageView cloneBtn = (ImageView) row
                            .findViewById(R.id.btn_clone_proj);
            cloneBtn.setOnClickListener(mCloneListener);
            cloneBtn.setTag(position);

            ImageView changeSDK = (ImageView) row
                            .findViewById(R.id.btn_change_sdk);
            changeSDK.setOnClickListener(mChangeSDKListener);
            changeSDK.setTag(position);

            ImageView renameBtn = (ImageView) row
                            .findViewById(R.id.btn_rename_proj);
            renameBtn.setOnClickListener(mRenameListener);
            renameBtn.setTag(position);

            return row;
        }
    }

    private String[] FindAvailableProjectNames() {
        String path = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/";

        File f = new File(path);
        return f.list();
    }

    public void hideAllActionButtons() {
        for (int i = 0; i < projectsList.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) projectsList.getChildAt(i);
            for (int j = 0; j < item.getChildCount(); j++) {
                View temp = item.getChildAt(j);
                if (temp instanceof ImageView) {
                    temp.setVisibility(View.INVISIBLE);
                }
            }
        }

    }

    public void showDetails(ProjectInfo projectInfo) {
        FrameLayout detailsFrame = (FrameLayout) findViewById(R.id.project_details_frame);
        ObjectAnimator.ofFloat(detailsFrame, "alpha", 1f).setDuration(100)
                        .start();

        TextView projName = (TextView) findViewById(R.id.thisProjectName);
        projName.setText(projectInfo.name);

        TextView targetSDK = (TextView) findViewById(R.id.projectSDK);
        targetSDK.setText(projectInfo.targetSDK);

        TextView author = (TextView) findViewById(R.id.projectAuthor);
        author.setText(projectInfo.author);

        TextView mainAct = (TextView) findViewById(R.id.projectMainActName);
        mainAct.setText(projectInfo.mainActivityName);
    }

    private class DeleteListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            final String projectName = (String) projectsList
                            .getItemAtPosition(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(
                            ProjectManagerActivity.this);
            builder.setMessage("Are you sure you want delete this project?")
                            .setCancelable(false)
                            .setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int id) {
                                                    deleteProject(projectName);
                                                }
                                            })
                            .setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int id) {
                                                    dialog.cancel();
                                                }
                                            }).show();
        }
    }

    private void deleteProject(String projectName) {
        String deletePath = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + projectName + "/";
        File f = new File(deletePath);
        if (f.exists()) {
            FileHelper.deleteDir(f);
        }

        refreshProjectList();
    }

    public void refreshProjectList() {
        projectNames = FindAvailableProjectNames();
        projectsList.setAdapter(new ProjectListAdapter(projectNames));
    }

    private class CloneListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            final View sizeView = getLayoutInflater().inflate(
                            R.layout.new_project_dialog, null);

            int position = (Integer) v.getTag();

            final String projectName = (String) projectsList
                            .getItemAtPosition(position);

            // dissapear the unwanted fields
            LinearLayout unimportant = (LinearLayout) sizeView
                            .findViewById(R.id.linearLayout5);
            unimportant.setVisibility(ViewGroup.GONE);
            LinearLayout unimportant2 = (LinearLayout) sizeView
                            .findViewById(R.id.linearLayout2);
            unimportant2.setVisibility(ViewGroup.GONE);
            LinearLayout unimportant3 = (LinearLayout) sizeView
                            .findViewById(R.id.linearLayout3);
            unimportant3.setVisibility(ViewGroup.GONE);
            LinearLayout unimportant4 = (LinearLayout) sizeView
                            .findViewById(R.id.linearLayout4);
            unimportant4.setVisibility(ViewGroup.GONE);
            LinearLayout unimportant5 = (LinearLayout) sizeView
                            .findViewById(R.id.LinearLayout01);
            unimportant5.setVisibility(ViewGroup.GONE);

            new AlertDialog.Builder(ProjectManagerActivity.this)
                            .setTitle("Clone Project...")
                            .setView(sizeView)
                            .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int whichbutton) {
                                                    EditText projName = (EditText) sizeView
                                                                    .findViewById(R.id.new_proj_name);

                                                    if (projName.getText()
                                                                    .toString()
                                                                    .isEmpty()) {
                                                        Toast.makeText(ProjectManagerActivity.this,
                                                                        "Invalid project name!",
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                        dialog.cancel();
                                                    }

                                                    cloneProject(projectName,
                                                                    projName.getText()
                                                                                    .toString());
                                                }
                                            })
                            .setNegativeButton("Cancel", null).show();
        }
    }

    private void cloneProject(String fromName, String toName) {
        String fromPath = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + fromName + "/";
        String toPath = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + toName + "/";

        File f = new File(fromPath);
        File toF = new File(toPath);

        try {
            FileHelper.copyDirectory(f, toF);
        } catch (IOException e) {
            // Log.d("Copy failure", "File or directory does not exist.");
            Toast.makeText(ProjectManagerActivity.this,
                            "Copy failure! File or directory does not exist.",
                            Toast.LENGTH_SHORT).show();
        }

        renameClonedProjectFile(fromName, toName);

        ProjectXMLParser parser = new ProjectXMLParser(toName, this);
        parser.StartParser();
        parser.renameClonedProject(fromName, toName);

        refreshProjectList();
    }

    private void renameClonedProjectFile(String fromName, String toName) {
        try {
            String oldFilePath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + toName + "/" + fromName
                            + ".xml";

            String newFilePath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + toName + "/" + toName
                            + ".xml";

            File f = new File(oldFilePath);
            File newF = new File(newFilePath);
            f.renameTo(newF);
        } catch (Exception e) {

        }
    }

    private class RenameListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            final View renameView = getLayoutInflater().inflate(
                            R.layout.rename_dialog, null);

            final TextView currName = (TextView) renameView
                            .findViewById(R.id.rename_dialog_curr_name);
            int position = (Integer) v.getTag();

            currName.setText((CharSequence) projectsList
                            .getItemAtPosition(position));

            new AlertDialog.Builder(ProjectManagerActivity.this)
                            .setTitle("Rename Activity...")
                            .setView(renameView)
                            .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int whichbutton) {
                                                    EditText newname = (EditText) renameView
                                                                    .findViewById(R.id.rename_dialog_edittext);
                                                    CharSequence toSet = (CharSequence) newname
                                                                    .getText();
                                                    if (toSet.length() == 0) {
                                                        Toast.makeText(ProjectManagerActivity.this,
                                                                        "Cannot set empty name! Made no changes.",
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                    } else if (projectExistsAlready(toSet
                                                                    .toString())) {
                                                        Toast.makeText(ProjectManagerActivity.this,
                                                                        "Name already exists!",
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                    } else {
                                                        renameProject(currName
                                                                        .getText()
                                                                        .toString(),
                                                                        toSet.toString());
                                                    }
                                                }
                                            })
                            .setNegativeButton("Cancel", null).show();
        }
    }

    private void renameProject(String oldName, String newName) {
        ProjectXMLParser parser = new ProjectXMLParser(oldName, this);
        parser.StartParser();
        parser.renameProject(oldName, newName);

        refreshProjectList();
    }

    private boolean projectExistsAlready(String projName) {
        for (String str : projectNames) {
            if (str.equals(projName)) {
                return true;
            }
        }
        return false;
    }

    private ProjectInfo findProjectInfoByName(String projectName) {
        ProjectXMLParser parser = new ProjectXMLParser(projectName, this);
        parser.StartParser();
        return parser.GetProjectInfo();
    }

    private class ChangeSDKListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            final View sizeView = getLayoutInflater().inflate(
                            R.layout.change_sdk_dialog, null);

            int position = (Integer) v.getTag();
            final String projectName = projectsList.getItemAtPosition(position)
                            .toString();

            final ProjectInfo proj = findProjectInfoByName(projectName);

            final TextView currSDK = (TextView) sizeView
                            .findViewById(R.id.activity_size_label2);
            currSDK.setText(proj.targetSDK);

            Spinner sdkValuesSpinner = (Spinner) sizeView
                            .findViewById(R.id.new_sdk_target);
            sdkValuesSpinner.setTag(projectName); // gotta save our project name
                                                  // or
            // we're going out of scope
            ArrayAdapter<CharSequence> adapter2 = ArrayAdapter
                            .createFromResource(
                                            ProjectManagerActivity.this,
                                            R.array.SDKValues,
                                            android.R.layout.simple_spinner_item);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sdkValuesSpinner.setAdapter(adapter2);

            new AlertDialog.Builder(ProjectManagerActivity.this)
                            .setTitle("Change target SDK...")
                            .setView(sizeView)
                            .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int whichbutton) {

                                                    Spinner newSDK = (Spinner) sizeView
                                                                    .findViewById(R.id.new_sdk_target);

                                                    String projectName = newSDK
                                                                    .getTag()
                                                                    .toString();
                                                    changeTargetSDK(projectName,
                                                                    newSDK.getSelectedItem()
                                                                                    .toString());
                                                }
                                            })
                            .setNegativeButton("Cancel", null).show();
        }
    }

    private class EditListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                int position = (Integer) v.getTag();
                final String projectName = (String) projectsList
                                .getItemAtPosition(position);

                // Load the main activity in the project
                LoadProjectMainActivity(projectName);
            } catch (Exception e) {
                // Log.d("edit activity", e.getMessage());
                Toast.makeText(ProjectManagerActivity.this, "Couldn't load project!",
                                Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void LoadProjectMainActivity(String projectName) {
        try {
            // Parse all project and activity info from the XMLs
            ProjectXMLParser parser = new ProjectXMLParser(projectName, this);
            parser.StartParser();
            ProjectInfo project = parser.GetProjectInfo();
            ActivityInfo mainAct = parser
                            .GetActivityInfo(project.mainActivityName);

            // Load the main activity in the project
            Intent i = new Intent(this, EditorActivity.class);
            i.putExtra("project", project);
            i.putExtra("activity", mainAct);
            i.putExtra("projectLoaderFlag", true);
            startActivity(i);
        } catch (Exception e) {
            // Log.d("Loading", e.getMessage());
            Toast.makeText(this, "Couldn't load project!", Toast.LENGTH_SHORT)
                            .show();
        }
    }

    private void changeTargetSDK(String projectName, String newSDKTarget) {
        ProjectXMLParser parser = new ProjectXMLParser(projectName, this);
        parser.StartParser();
        parser.changeProjectSDK(newSDKTarget);

        refreshProjectList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return (true);
        }
        return super.onOptionsItemSelected(item);
    }
}
