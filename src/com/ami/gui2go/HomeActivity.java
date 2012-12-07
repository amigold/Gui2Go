package com.ami.gui2go;

import java.io.File;

import com.ami.gui2go.models.ActivityInfo;
import com.ami.gui2go.models.ProjectInfo;
import com.ami.gui2go.utils.FileHelper;
import com.ami.gui2go.utils.ProjectXMLParser;
import com.ami.gui2go.utils.TextValidator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
    public class ProjectItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                        long id) {
            ProjectXMLParser parser = new ProjectXMLParser(
                            projectNames[position], HomeActivity.this);
            parser.StartParser();
            ProjectInfo project = parser.GetProjectInfo();

            LoadProjectMainActivity(project.name);
        }
    }

    // Grab UI items
    ListView recentList;

    String[] projectNames;
    String[] recentNames;

    public void createNewProject(View v) {
        final View sizeView = getLayoutInflater().inflate(
                        R.layout.new_project_dialog, null);

        // set targetSDK spinner values
        Spinner targetSDK = (Spinner) sizeView
                        .findViewById(R.id.new_projc_sdk_Spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                        this, R.array.SDKValues,
                        android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetSDK.setAdapter(adapter1);

        // set window size spinner values
        Spinner windowSize = (Spinner) sizeView
                        .findViewById(R.id.new_proj_size_Spinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                        this, R.array.screenSizes,
                        android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        windowSize.setAdapter(adapter2);

        // set root layout types spinner
        final Spinner layoutType = (Spinner) sizeView
                        .findViewById(R.id.new_proj_layout_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.rootLayoutTypes,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutType.setAdapter(adapter);

        new AlertDialog.Builder(this)
                        .setTitle("New Project..")
                        .setView(sizeView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                EditText projName = (EditText) sizeView
                                                                .findViewById(R.id.new_proj_name);
                                                EditText mainActName = (EditText) sizeView
                                                                .findViewById(R.id.new_proj_main_act);
                                                Spinner targetSDK = (Spinner) sizeView
                                                                .findViewById(R.id.new_projc_sdk_Spinner);
                                                Spinner windowSize = (Spinner) sizeView
                                                                .findViewById(R.id.new_proj_size_Spinner);
                                                EditText author = (EditText) sizeView
                                                                .findViewById(R.id.new_proj_author);

                                                // validation
                                                boolean shouldProceed = true;
                                                if (!TextValidator
                                                                .isNameFieldValid(mainActName
                                                                                .getText()
                                                                                .toString())) {
                                                    Toast.makeText(HomeActivity.this,
                                                                    "Invalid activity name!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    shouldProceed = false;
                                                    dialog.cancel();
                                                }
                                                if (!TextValidator
                                                                .isNameFieldValid(projName
                                                                                .getText()
                                                                                .toString())) {
                                                    Toast.makeText(HomeActivity.this,
                                                                    "Invalid project name!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    shouldProceed = false;
                                                    dialog.cancel();
                                                }
                                                // ///
                                                ProjectInfo newProject = new ProjectInfo();
                                                ActivityInfo mainAct = new ActivityInfo();

                                                newProject.name = projName
                                                                .getText()
                                                                .toString();
                                                newProject.targetSDK = targetSDK
                                                                .getSelectedItem()
                                                                .toString();
                                                newProject.mainActivityName = mainActName
                                                                .getText()
                                                                .toString();
                                                newProject.author = author
                                                                .getText()
                                                                .toString();
                                                mainAct.name = mainActName
                                                                .getText()
                                                                .toString();
                                                mainAct.screenSize = windowSize
                                                                .getSelectedItem()
                                                                .toString();

                                                String[] projects = FindAvailableProjectNames();

                                                if (projects != null) {
                                                    // check if project exists
                                                    // already
                                                    for (int i = 0; i < projects.length; i++) {
                                                        if (projects[i].equals(newProject.name)) {
                                                            Toast.makeText(HomeActivity.this,
                                                                            "A project by this name already exists!",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();
                                                            shouldProceed = false;
                                                        }
                                                    }
                                                }

                                                // check if the activity and
                                                // project name are the same
                                                if (newProject.name
                                                                .equals(mainAct.name)) {
                                                    Toast.makeText(HomeActivity.this,
                                                                    "Identical project/activity names are not allowed!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    shouldProceed = false;
                                                }

                                                if (shouldProceed == true) {
                                                    ProjectXMLParser parser = new ProjectXMLParser(
                                                                    newProject.name,
                                                                    HomeActivity.this);
                                                    parser.CreateNewProjectXML(
                                                                    newProject,
                                                                    mainAct);

                                                    Intent i = new Intent(
                                                                    HomeActivity.this,
                                                                    EditorActivity.class);
                                                    i.putExtra("project",
                                                                    newProject);
                                                    i.putExtra("activity",
                                                                    mainAct);
                                                    i.putExtra("isNewActivity",
                                                                    true);
                                                    i.putExtra("layoutType",
                                                                    layoutType.getSelectedItem()
                                                                                    .toString());
                                                    startActivity(i);
                                                }
                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    public void doFileItemClick(final String fileName) {
        final View sizeView = getLayoutInflater().inflate(
                        R.layout.new_project_dialog, null);

        // set targetSDK spinner values
        Spinner targetSDK = (Spinner) sizeView
                        .findViewById(R.id.new_projc_sdk_Spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                        this, R.array.SDKValues,
                        android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetSDK.setAdapter(adapter1);
        // set window size spinner values
        Spinner windowSize = (Spinner) sizeView
                        .findViewById(R.id.new_proj_size_Spinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                        this, R.array.screenSizes,
                        android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        windowSize.setAdapter(adapter2);

        // set root layout types spinner
        final Spinner layoutType = (Spinner) sizeView
                        .findViewById(R.id.new_proj_layout_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.rootLayoutTypes,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutType.setAdapter(adapter);

        new AlertDialog.Builder(this)
                        .setTitle("New Project..")
                        .setView(sizeView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                EditText projName = (EditText) sizeView
                                                                .findViewById(R.id.new_proj_name);
                                                EditText mainActName = (EditText) sizeView
                                                                .findViewById(R.id.new_proj_main_act);
                                                Spinner targetSDK = (Spinner) sizeView
                                                                .findViewById(R.id.new_projc_sdk_Spinner);
                                                Spinner windowSize = (Spinner) sizeView
                                                                .findViewById(R.id.new_proj_size_Spinner);
                                                EditText author = (EditText) sizeView
                                                                .findViewById(R.id.new_proj_author);

                                                // validation
                                                boolean shouldProceed = true;
                                                if (!TextValidator
                                                                .isNameFieldValid(mainActName
                                                                                .getText()
                                                                                .toString())) {
                                                    Toast.makeText(HomeActivity.this,
                                                                    "Invalid activity name!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    shouldProceed = false;
                                                    dialog.cancel();
                                                }
                                                if (!TextValidator
                                                                .isNameFieldValid(projName
                                                                                .getText()
                                                                                .toString())) {
                                                    Toast.makeText(HomeActivity.this,
                                                                    "Invalid project name!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    shouldProceed = false;
                                                    dialog.cancel();
                                                }
                                                // ///
                                                ProjectInfo newProject = new ProjectInfo();
                                                ActivityInfo mainAct = new ActivityInfo();

                                                newProject.name = projName
                                                                .getText()
                                                                .toString();
                                                newProject.targetSDK = targetSDK
                                                                .getSelectedItem()
                                                                .toString();
                                                newProject.mainActivityName = mainActName
                                                                .getText()
                                                                .toString();
                                                newProject.author = author
                                                                .getText()
                                                                .toString();
                                                mainAct.name = mainActName
                                                                .getText()
                                                                .toString();
                                                mainAct.screenSize = windowSize
                                                                .getSelectedItem()
                                                                .toString();

                                                String[] projects = FindAvailableProjectNames();

                                                // check if project exists
                                                // already
                                                for (int i = 0; i < projects.length; i++) {
                                                    if (projects[i].equals(newProject.name)) {
                                                        Toast.makeText(HomeActivity.this,
                                                                        "A project by this name already exists!",
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                        shouldProceed = false;
                                                    }
                                                }

                                                // check if the activity and
                                                // project name are the same
                                                if (newProject.name
                                                                .equals(mainAct.name)) {
                                                    Toast.makeText(HomeActivity.this,
                                                                    "Identical project/activity names are not allowed!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    shouldProceed = false;
                                                }

                                                if (shouldProceed == true) {
                                                    ProjectXMLParser parser = new ProjectXMLParser(
                                                                    newProject.name,
                                                                    HomeActivity.this);
                                                    parser.CreateNewProjectXML(
                                                                    newProject,
                                                                    mainAct);

                                                    File from = new File(
                                                                    fileName);
                                                    File to = new File(
                                                                    Environment.getExternalStorageDirectory()
                                                                                    + "/Gui2Go/Projects/"
                                                                                    + newProject.name
                                                                                    + "/"
                                                                                    + mainAct.name
                                                                                    + ".xml");
                                                    try {
                                                        FileHelper.copy(from,
                                                                        to);
                                                    } catch (Exception e) {
                                                        // Log.d("Import XML",
                                                        // "Error");
                                                        Toast.makeText(HomeActivity.this,
                                                                        "Error importing XML!",
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                    }
                                                }
                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    public void exportProject() {
        // TODO fill it up
    }

    private String[] FindAvailableProjectNames() {
        String path = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/";

        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f.list();
    }

    public void importProject(View v) {
        showFileOpenDialog();
    }

    private boolean isFirstLaunch() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences("prefs", 0);
        boolean isFirstLaunch = settings.getBoolean("isFirstLaunch", true);

        return isFirstLaunch;
    }

    public void LoadProject(View v) {
        final String[] projects = FindAvailableProjectNames();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Project:");
        builder.setItems(projects, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                LoadProjectMainActivity(projects[item]);
            }
        }).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Init the activity
        recentList = (ListView) findViewById(R.id.recentsList);
        recentList.setOnItemClickListener(new ProjectItemClickListener());
        recentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        TextView emptyText = new TextView(this);
        emptyText.setText("No Projects!");
        emptyText.setTextSize(24);
        recentList.setEmptyView(emptyText);

        UpdateRecentsList();

        setTitle("");

        // showFirstTimeHelpDialog();
        if (isFirstLaunch()) {
            showFirstTimeHelpDialog();

            // Save the preferences, isFirstLaunch will now be false
            SharedPreferences settings = getSharedPreferences("prefs", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.manage_projects_menu:
            openProjectManager();
            return (true);
        case R.id.usage_guide_menu:
            openUserGuide();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateRecentsList();
    }

    private void openProjectManager() {
        Intent i = new Intent(this, ProjectManagerActivity.class);
        startActivity(i);
    }

    private void openUserGuide() {
        Intent i = new Intent(this, UserGuideActivity.class);
        startActivity(i);
    }

    private void showFileOpenDialog() {
        String path = Environment.getExternalStorageDirectory() + "/";
        FileDialogFragment newFragment = FileDialogFragment.newInstance(path);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void showFirstTimeHelpDialog() {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.first_help_dialog,
                        (ViewGroup) findViewById(R.id.root));

        TextView text = (TextView) layout.findViewById(R.id.help2);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setTitle("Welcome to Gui 2 Go!");
        builder.setPositiveButton("Ok, take me to the guide!",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                            int which) {
                                openUserGuide();
                            }
                        });
        builder.setNegativeButton("No thanks, I'm actually a Pro in disguise",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                            int which) {
                                dialog.cancel();
                            }
                        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressWarnings("unchecked")
    private void UpdateRecentsList() {
        ArrayAdapter<String> adapter = null;

        projectNames = FindAvailableProjectNames();

        if (projectNames == null)
            return;

        if (projectNames.length > 5) {
            recentNames = new String[5];
            for (int i = 0; i < 5; i++) {
                recentNames[i] = projectNames[i];
            }
            adapter = new ArrayAdapter<String>(this,
                            R.layout.small_list_item_row, recentNames);
            recentList.setAdapter(adapter);
            ((ArrayAdapter<String>) recentList.getAdapter())
                            .notifyDataSetChanged();
        } else if (projectNames.length > 0) {
            adapter = new ArrayAdapter<String>(this,
                            R.layout.small_list_item_row, projectNames);
            recentList.setAdapter(adapter);
            ((ArrayAdapter<String>) recentList.getAdapter())
                            .notifyDataSetChanged();
        } else { // its empty
            adapter = new ArrayAdapter<String>(this,
                            R.layout.small_list_item_row, projectNames);
            recentList.setAdapter(adapter);
            ((ArrayAdapter<String>) recentList.getAdapter())
                            .notifyDataSetInvalidated();
        }
    }
}
