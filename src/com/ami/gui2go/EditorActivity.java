package com.ami.gui2go;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.util.Xml;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.ami.gui2go.models.ActivityInfo;
import com.ami.gui2go.models.ProjectInfo;
import com.ami.gui2go.models.Widget;
import com.ami.gui2go.models.WidgetAttribute;
import com.ami.gui2go.models.WidgetTypes;
import com.ami.gui2go.models.WidgetWrapper;
import com.ami.gui2go.tree.InMemoryTreeStateManager;
import com.ami.gui2go.tree.SimpleStandardAdapter;
import com.ami.gui2go.tree.TreeStateManager;
import com.ami.gui2go.tree.TreeViewList;
import com.ami.gui2go.utils.FileHelper;
import com.ami.gui2go.utils.LayoutXMLCreator;
import com.ami.gui2go.utils.ProjectXMLParser;
import com.ami.gui2go.utils.TextValidator;
import com.ami.gui2go.views.ColorPickerDialog;

public class EditorActivity extends Activity implements OnNavigationListener {
    ActivityInfo activityInfo;
    private ProjectInfo projectInfo;
    ArrayList<ActivityInfo> activityList;
    private ArrayList<String> activities;
    private int currentHighlightedID;
    private int currentSelectedGridItemId;
    private int tempHighlightedId;
    private boolean isRelativeUtilityOpen = false;
    private boolean isAnimationInProgress = false;
    private boolean wasDestroyedByWidget = true;
    private View viewHolder;
    private int relativePosition;
    private int lastWidgetID = 1000;
    private int lastWidgetDisplayID = 0;
    private int lastLayoutDisplayID = 0;
    private int maxHeight = 0;
    private int maxWidth = 0;
    public static ActionMode mode;
    private ArrayList<CharSequence> widgetNames = new ArrayList<CharSequence>();
    private ArrayList<Integer> widgetIDs = new ArrayList<Integer>();
    private ArrayAdapter<String> mSpinnerAdapter;

    private boolean isToolboxOpen = false;
    private boolean isTreeOpen = false;
    private boolean wasToolboxLastOpen = true;

    // TREE STUFF
    private SimpleStandardAdapter simpleAdapter;
    private TreeViewList treeView;
    private final TreeStateManager<Long> manager = new InMemoryTreeStateManager<Long>();
    private static final int LEVEL_NUMBER = 4;
    private final java.util.HashSet<Long> selected = new java.util.HashSet<Long>();
    // ///////////

    public Map<Integer, String> imageDictionary;
    
    // Views
    ViewGroup mainlay;
    GridView toolbox;
    LinearLayout treeDrawer;
    ViewFlipper flipper;
    TextView actionDescriber;
    RelativeLayout UICanvas;
    ImageButton widgetAddButton;
    ImageButton widgetDeleteButton;
    ImageButton widgetUpButton;
    ImageButton widgetDownButton;
    LinearLayout relativeUtilitiesLayout;
    LinearLayout toolboxLinearLayout;
    ImageView editAreaBackground;

    // toolbox\tree buttons
    ImageButton toolboxButton;
    ImageButton treeButton;

    // radial menu stuff
    ImageView radialMenuOpener;
    FrameLayout radialMenuLayout;
    ArrayList<View> menuItems;
    AnimatorSet menuAnimSet;
    boolean isDragging = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        InitEditorFramework();

        // Grab info from the intent
        Bundle data = getIntent().getExtras();
        projectInfo = data.getParcelable("project");
        activityInfo = data.getParcelable("activity");
        String layoutType = data.getString("layoutType");
        this.setTitle(projectInfo.name + " - ");
        if (data.getBoolean("projectLoaderFlag")) {
            // if this activity is loaded from the project loader
            // we need to load the correct layout XML
            LoadXML(projectInfo.name, activityInfo.name);
        }
        if (data.getBoolean("isNewActivity")) {
            if (!layoutType.isEmpty()) {
                ClearScreen(layoutType);
            }
            saveActivityXML(false);
        }

        // Init the action bar spinner (for quick activity selection)
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        refreshEditorData(false);

        // set the correct size
        setScreenSize(activityInfo.screenSize);

        // gridview test
        // toolbox.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        toolbox.setDrawSelectorOnTop(true);
        toolbox.setSelector(R.drawable.toolbox_border);
        toolbox.setOnItemLongClickListener(new GridViewItemClickListener());
        toolbox.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                if (currentSelectedGridItemId == -1) {
                    // need to select
                    currentSelectedGridItemId = position;
                } else if (currentSelectedGridItemId == position) {
                    // need to deselect
                    toolbox.setSelection(-1);
                    currentSelectedGridItemId = -1;
                } else {
                    // select new item
                    currentSelectedGridItemId = position;
                }
                toggleAddButton();
            }
        });
    }

    private void refreshEditorData(boolean isNewIntent) {
        activityList = GetAvailableActivities();
        activities = GetActivityNames(activityList);
        mSpinnerAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        activities);
        
        getActionBar().setListNavigationCallbacks(mSpinnerAdapter, this);
        
        if (!isNewIntent) {
            // make sure the selected activity is the current one
            for (int i = 0; i < activityList.size(); i++) {
                if (activityList.get(i).name.equals(activityInfo.name)) {
                    getActionBar().setSelectedNavigationItem(i);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long itemId) {
        if (activityList.get(position).name.equals(activityInfo.name)) {

        } else {
            if (EditorActivity.mode != null) {
                EditorActivity.mode.finish();
            }
            showCloseSaveDialogAndChangeActivity(position);
        }
        return true;
    }

    private ArrayList<ActivityInfo> GetAvailableActivities() {
        ProjectXMLParser parser = new ProjectXMLParser(projectInfo.name, this);
        parser.StartParser();
        ArrayList<ActivityInfo> activityList = parser.GetActivityList();
        return activityList;
    }

    private ArrayList<String> GetActivityNames(
                    ArrayList<ActivityInfo> activityList) {
        ArrayList<String> res = new ArrayList<String>();
        for (ActivityInfo activityInfo : activityList) {
            res.add(activityInfo.name);
        }
        return res;
    }

    private void InitEditorFramework() {
        // Wire ups
        OnDragListener mDragListen = new LayoutDragListener(); // for listening
        // to drag drops

        UICanvas = (RelativeLayout) findViewById(R.id.UIcanvas); // father of
        // mainlay
        UICanvas.setTag("MAIN_CANVAS");
        mainlay = (LinearLayout) findViewById(R.id.mainLayout);
        mainlay.setOnDragListener(mDragListen);

        flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
        editAreaBackground = (ImageView) findViewById(R.id.ui_bg_img);

        toolbox = (GridView) findViewById(R.id.toolbox);
        toolbox.setAdapter(new WidgetAdapter());

        actionDescriber = (TextView) findViewById(R.id.actionDescriber);

        // utility panels
        relativeUtilitiesLayout = (LinearLayout) findViewById(R.id.relativeUtilitiesLayout);
        toolboxLinearLayout = (LinearLayout) findViewById(R.id.toolboxPanelLayout);

        // icons below the tree\toolbox
        widgetAddButton = (ImageButton) findViewById(R.id.widgetAddButton);
        widgetDeleteButton = (ImageButton) findViewById(R.id.widgetDeleteButton);
        widgetDeleteButton.setOnDragListener(new DeleteDragListener());
        widgetUpButton = (ImageButton) findViewById(R.id.widgetUpButton);
        widgetDownButton = (ImageButton) findViewById(R.id.widgetDownButton);

        // create the root layout in the tree
        long rootID = (long) mainlay.getId();
        mainlay.setTag("LayoutRoot");
        manager.addAfterChild(null, rootID, null);
        // createTestButtons();

        treeView = new TreeViewList(this);
        treeView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
        treeView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        simpleAdapter = new SimpleStandardAdapter(this, selected, manager,
                        LEVEL_NUMBER);
        treeView.setAdapter(simpleAdapter);
        flipper.addView(treeView);
        // registerForContextMenu(treeView);
        // ////////
        currentHighlightedID = 0;
        currentSelectedGridItemId = -1;

        toolboxButton = (ImageButton) findViewById(R.id.toolboxBtn);
        treeButton = (ImageButton) findViewById(R.id.treeBtn);

        // radial menu stuff
        radialMenuOpener = (ImageView) findViewById(R.id.radialMenuButton);
        // radialMenuOpener.setOnTouchListener(new
        // RadialMenuOpenerTouchListener());
        radialMenuLayout = (FrameLayout) findViewById(R.id.radialMenuLayout);
        radialMenuLayout.setOnTouchListener(new RadialMenuItemTouchListener());

        menuItems = new ArrayList<View>();
        for (int i = 0; i < radialMenuLayout.getChildCount(); i++) {
            menuItems.add(radialMenuLayout.getChildAt(i));
        }

        imageDictionary = new HashMap<Integer, String>();
    }

    private View findView(float x, float y, ArrayList<View> targets) {
        final int count = targets.size();
        for (int i = 0; i < count; i++) {
            final View target = targets.get(i);
            if (target.getRight() > x && target.getTop() < y
                            && target.getBottom() > y && target.getLeft() < x) {
                return target;
            }
        }
        return null;
    }

    class RadialMenuItemTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            View foundTarget = null;
            foundTarget = findView(event.getX(), event.getY(), menuItems);

            if (event.getAction() == MotionEvent.ACTION_DOWN
                            && foundTarget != null) {
                if (foundTarget.getTag().toString().equals("Menu")) {
                    showRadialMenu();
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                hideRadialMenu();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (foundTarget != null
                                && !foundTarget.getTag().toString()
                                                .equals("Menu") && !isDragging) {
                    String tag = foundTarget.getTag().toString();
                    String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };

                    ClipData.Item item = new ClipData.Item(tag);
                    ClipData dragData = new ClipData("GridViewItem", mimeTypes,
                                    item);

                    View.DragShadowBuilder myShadow = new DragShadowBuilder(
                                    foundTarget);

                    foundTarget.startDrag(dragData, myShadow, null, 0);
                    isDragging = true;
                    return true;
                }
            }
            return false;
        }
    }

    protected void hideRadialMenu() {
        menuAnimSet.end();
        // should be in reverse order
        List<Animator> animList = new ArrayList<Animator>();
        // now we add them all to the anim list
        for (int i = radialMenuLayout.getChildCount() - 1; i > 0; i--) {
            if (radialMenuLayout.getChildAt(i).getId() != radialMenuOpener
                            .getId()) {
                // make sure its not the menu opener
                View v = radialMenuLayout.getChildAt(i);
                AnimatorSet animSet = new AnimatorSet();
                ArrayList<Animator> viewAnimList = new ArrayList<Animator>();

                Animator anim = ObjectAnimator.ofFloat(v, "alpha", 0f)
                                .setDuration(100);
                viewAnimList.add(anim);
                anim = ObjectAnimator.ofFloat(v, "scaleX", 0f).setDuration(100);
                viewAnimList.add(anim);
                anim = ObjectAnimator.ofFloat(v, "scaleY", 0f).setDuration(100);
                viewAnimList.add(anim);

                animSet.playTogether(viewAnimList);
                animList.add(animSet);
            }
        }

        menuAnimSet = new AnimatorSet();
        menuAnimSet.playSequentially(animList);
        menuAnimSet.start();
        isDragging = false; // make sure you are not considered dragging anymore
    }

    protected void showRadialMenu() {
        List<Animator> animList = new ArrayList<Animator>();
        // now we add them all to the anim list
        for (int i = 0; i < radialMenuLayout.getChildCount(); i++) {
            if (radialMenuLayout.getChildAt(i).getId() != radialMenuOpener
                            .getId()) {
                // make sure its not the menu opener
                View v = radialMenuLayout.getChildAt(i);
                AnimatorSet animSet = new AnimatorSet();
                ArrayList<Animator> viewAnimList = new ArrayList<Animator>();

                Animator anim = ObjectAnimator.ofFloat(v, "alpha", 1f)
                                .setDuration(100);
                viewAnimList.add(anim);
                anim = ObjectAnimator.ofFloat(v, "scaleX", 1f).setDuration(100);
                viewAnimList.add(anim);
                anim = ObjectAnimator.ofFloat(v, "scaleY", 1f).setDuration(100);
                viewAnimList.add(anim);

                animSet.playTogether(viewAnimList);
                animList.add(animSet);
            }
        }

        menuAnimSet = new AnimatorSet();
        menuAnimSet.playSequentially(animList);
        menuAnimSet.start();
    }

    private void setScreenSize(String screenSize) {
        String[] sizes = getResources().getStringArray(R.array.screenSizes);
        if (screenSize.equals(sizes[0])) {
            // 2.7in QVGA 240x320
            editAreaBackground.setImageResource(R.drawable.bg240x320);
            maxWidth = 240;
            maxHeight = 320;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[1])) {
            // 2.7in QVGA 240x320
            editAreaBackground.setImageResource(R.drawable.bg240x320_land);
            maxHeight = 240;
            maxWidth = 320;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[2])) {
            // 3.2in HVGA 320x480
            editAreaBackground.setImageResource(R.drawable.bg320x480);
            maxWidth = 320;
            maxHeight = 480;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[3])) {
            // 3.2in HVGA 320x480
            editAreaBackground.setImageResource(R.drawable.bg320x480_land);
            maxWidth = 480;
            maxHeight = 320;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[4])) {
            // 3.3in WQVGA 240x400
            editAreaBackground.setImageResource(R.drawable.bg240x400);
            maxWidth = 240;
            maxHeight = 400;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[5])) {
            // 3.3in WQVGA 240x400
            editAreaBackground.setImageResource(R.drawable.bg240x400_land);
            maxWidth = 400;
            maxHeight = 240;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[6])) {
            // 3.4in WQVGA 240x432
            editAreaBackground.setImageResource(R.drawable.bg240x432);
            maxWidth = 240;
            maxHeight = 432;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[7])) {
            // 3.4in WQVGA 240x432
            editAreaBackground.setImageResource(R.drawable.bg240x432_land);
            maxWidth = 432;
            maxHeight = 240;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[8])) {
            // 5.1in WVGA / 3.7in WVGA 480x800
            editAreaBackground.setImageResource(R.drawable.bg480x800);
            maxWidth = 480;
            maxHeight = 800;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[9])) {
            // 5.1in WVGA / 3.7in WVGA 480x800
            editAreaBackground.setImageResource(R.drawable.bg480x800_land);
            maxWidth = 800;
            maxHeight = 480;
            radialMenuLayout.setVisibility(View.GONE);
        } else if (screenSize.equals(sizes[10])) {
            // 5.4 FWVGA / 3.7in FWVGA 480x854
            editAreaBackground.setImageResource(R.drawable.bg480x854);
            maxWidth = 480;
            maxHeight = 854;
            radialMenuLayout.setVisibility(View.VISIBLE);
        } else if (screenSize.equals(sizes[11])) {
            // 5.4 FWVGA / 3.7in FWVGA 480x854
            editAreaBackground.setImageResource(R.drawable.bg480x854_land);
            maxWidth = 854;
            maxHeight = 480;
            radialMenuLayout.setVisibility(View.GONE);
        } else if (screenSize.equals(sizes[12])) {
            // 10.1in WXGA 1280x800
            editAreaBackground.setImageResource(R.drawable.bg1280x768);
            maxWidth = 1280;
            maxHeight = 800;
        }
        // set the canvas size to fit the new background image, and center it
        // again
        UICanvas.setLayoutParams(new android.widget.RelativeLayout.LayoutParams(
                        maxWidth, maxHeight));
        ToggleLayoutParam(UICanvas, 13);
    }

    @SuppressWarnings("unused")
    private void createTestButtons() {
        // regular widget clicks - so we can highlight them
        // OnClickListener mClickListener = new HightlightViewClickListener();
        // // long click listener
        // OnLongClickListener mLongClickListener = new LongClickListener();
        //
        // // FOR TESTS
        // button = (Button) findViewById(R.id.button1);
        // registerForContextMenu(button);
        // button.setTag("LOL");
        // button.setOnClickListener(mClickListener);
        // button.setOnLongClickListener(mLongClickListener);
        //
        // text = (TextView) findViewById(R.id.textView1);
        //
        // button2 = (Button) findViewById(R.id.button2);
        // button3 = (Button) findViewById(R.id.button3);
        // lay2 = (LinearLayout) findViewById(R.id.lolers);
        // // TREE STUFF
        // long buttonID = (long) button.getId();
        // long textID = (long) text.getId();
        //
        // long buttonID2 = (long) button2.getId();
        // long buttonID3 = (long) button3.getId();
        // long lolersID = (long) lay2.getId();
        //
        // text.setTag("lols");
        // button.setTag("button");
        // button2.setTag("button2");
        // button3.setTag("button3");
        // lay2.setTag("HOLDIT");
        //
        // manager.addAfterChild(rootID, buttonID, null);
        // manager.addAfterChild(rootID, textID, null);
        // manager.addAfterChild(rootID, lolersID, null);
        // manager.addAfterChild(lolersID, buttonID2, null);
        // manager.addAfterChild(lolersID, buttonID3, null);
    }

    public void addButtonAction(View v) {
        LinearLayout selectedItem = (LinearLayout) toolbox
                        .getChildAt(currentSelectedGridItemId);
        if (selectedItem != null) {
            String tag = (String) ((TextView) selectedItem.getChildAt(0))
                            .getText(); // make sure theres a selection
            if (currentHighlightedID == 0) {
                // selection is empty, put it in the main layout
                createNewWidget(mainlay, tag, false, true, false, true);
            } else {
                View targetLayout = findViewById(currentHighlightedID);
                if (targetLayout instanceof ViewGroup) { // only if the selected
                                                         // item is a layout
                    createNewWidget(targetLayout, tag, false, true, false, true);
                }
            }
        } else {
            Toast.makeText(this, "Must select a widget first!",
                            Toast.LENGTH_SHORT).show();
        }
    }

    public void btnClick_widgetUp(View v) {
        // this is an ugly solution due to API lacking something to change Z
        // order
        // IMPORTANT: bring to front actually puts them in the bottom
        if (currentHighlightedID != 0) {
            View target = findViewById(currentHighlightedID);
            ViewGroup parent = (ViewGroup) target.getParent();
            int position = parent.indexOfChild(target);

            LinkedList<View> viewsToFront = new LinkedList<View>();
            if (position != 0) {
                // do it in the tree
                for (int i = 0; i < treeView.getCount(); i++) {
                    View viewFromTree = (View) treeView.getChildAt(i);
                    if (viewFromTree != null) {
                        String treeViewTag = viewFromTree.getTag().toString();

                        if (treeViewTag.equals(String.valueOf(target.getId()))) {
                            // before moving we should save all its children
                            List<Long> children = manager
                                            .getChildren((long) target.getId());

                            manager.removeNodeRecursively((long) target.getId());
                            insertViewToTreeParentAfterTarget(target, parent,
                                            parent.getChildAt(position - 1));
                            if (children != null) {
                                // re-add all its children
                                for (int j = 0; j < children.size(); j++) {
                                    View tempView = ((ViewGroup) target)
                                                    .getChildAt(j);
                                    manager.addAfterChild(
                                                    (long) target.getId(),
                                                    (long) tempView.getId(),
                                                    null);
                                }
                            }
                        }
                    }
                }

                // for (int i = 0; i < treeView.getCount(); i++) { // all this
                // to
                // // find the new
                // // tree view and
                // // highlight it
                // View viewFromTree = (View) treeView.getChildAt(i);
                // if (viewFromTree != null) {
                // String treeViewTag = viewFromTree.getTag().toString();
                // if (treeViewTag.equals(String.valueOf(target.getId()))) {
                // viewFromTree
                // .setBackgroundResource(R.drawable.hightlight_border);
                // }
                // }
                // }

                viewsToFront.add(parent.getChildAt(position - 1));
                for (int i = position + 1; i < parent.getChildCount(); i++) {
                    // store the views we need to bring up after
                    // up until the position of 2 before the target
                    viewsToFront.add(parent.getChildAt(i));
                }

                // now go over the stack and put em all on top in the correct
                // order
                while (!viewsToFront.isEmpty()) {
                    View temp = viewsToFront.pop();
                    parent.bringChildToFront(temp);
                }
                target.requestLayout();
            }
        } else {
            Toast.makeText(this, "You must select a widget first!",
                            Toast.LENGTH_SHORT).show();
        }
    }

    public void btnClick_widgetDown(View v) {
        if (currentHighlightedID != 0) {
            View target = findViewById(currentHighlightedID);
            ViewGroup parent = (ViewGroup) target.getParent();
            int position = parent.indexOfChild(target);

            LinkedList<View> viewsToFront = new LinkedList<View>();
            if (position != parent.getChildCount()) {
                // now do it in the tree
                for (int i = 0; i < treeView.getCount(); i++) {
                    View viewFromTree = (View) treeView.getChildAt(i);
                    if (viewFromTree != null) {
                        String treeViewTag = viewFromTree.getTag().toString();

                        if (treeViewTag.equals(String.valueOf(target.getId()))) {
                            // before moving we should save all its children
                            List<Long> children = manager
                                            .getChildren((long) target.getId());

                            manager.removeNodeRecursively((long) target.getId());
                            insertViewToTreeParentAfterTarget(target, parent,
                                            parent.getChildAt(position + 2));
                            if (children != null) {
                                // re-add all its children
                                for (int j = 0; j < children.size(); j++) {
                                    View tempView = ((ViewGroup) target)
                                                    .getChildAt(j);
                                    manager.addAfterChild(
                                                    (long) target.getId(),
                                                    (long) tempView.getId(),
                                                    null);
                                }
                            }
                        }
                    }
                }

                // for (int i = 0; i < treeView.getCount(); i++) { // all this
                // to
                // // find the new
                // // tree view and
                // // highlight it
                // View viewFromTree = (View) treeView.getChildAt(i);
                // if (viewFromTree != null) {
                // String treeViewTag = viewFromTree.getTag().toString();
                // if (treeViewTag.equals(String.valueOf(target.getId()))) {
                // viewFromTree
                // .setBackgroundResource(R.drawable.hightlight_border);
                // }
                // }
                // }

                viewsToFront.add(target);
                for (int i = position + 2; i < parent.getChildCount(); i++) {
                    // store the views we need to bring up after
                    // up until the position of 2 before the target
                    viewsToFront.add(parent.getChildAt(i));
                }

                // now go over the stack and put em all on top in the correct
                // order
                while (!viewsToFront.isEmpty()) {
                    View temp = viewsToFront.pop();
                    parent.bringChildToFront(temp);
                }
                target.requestLayout();
            }
        } else {
            Toast.makeText(this, "You must select a widget first!",
                            Toast.LENGTH_SHORT).show();
        }
    }

    public void btnClick_LeftOf(View v) {
        // Toast.makeText(EditorActivity.this, "Select a target widget",
        // Toast.LENGTH_SHORT).show();
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 0);
    }

    public void btnClick_RightOf(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 1);
    }

    public void btnClick_Above(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 2);
    }

    public void btnClick_Below(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 3);
    }

    public void btnClick_CenterInParent(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 13);
    }

    public void btnClick_AlignBaseline(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 4);
    }

    public void btnClick_AlignTop(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 6);
    }

    public void btnClick_AlignBottom(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 8);
    }

    public void btnClick_AlignLeft(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 5);
    }

    public void btnClick_AlignRight(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 7);
    }

    public void btnClick_AlignParentLeft(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 9);
    }

    public void btnClick_AlignParentRight(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 11);
    }

    public void btnClick_AlignParentTop(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 10);
    }

    public void btnClick_AlignParentBottom(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 12);
    }

    public void btnClick_CenterHorizontal(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 14);
    }

    public void btnClick_CenterVertical(View v) {
        View view = findViewById(currentHighlightedID);
        ToggleLayoutParam(view, 15);
    }

    public class HightlightViewLongClickListener implements OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            v.performClick();
            openContextMenu(v);
            return true;
        }

    }

    public class HightlightViewClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            performHightlight(v);
        }
    }

    public void performHightlight(View v) {
        if (!isAnimationInProgress) {
            if (v.getId() == currentHighlightedID) // exists already, we
                                                   // should
            // un-highlight
            {
                ClearHightlightedViews(false);
                ObjectAnimator.ofFloat(widgetDownButton, "alpha", 0f).start();
                ObjectAnimator.ofFloat(widgetUpButton, "alpha", 0f).start();
            } else if (currentHighlightedID != 0) {
                removeHighlight(findViewById(currentHighlightedID));
                currentHighlightedID = v.getId();
                highlightTargetView(v, "#FFFF66", R.drawable.hightlight_border,
                                true);

                // and finally restart the action mode
                wasDestroyedByWidget = false; // we use this global bool cause I
                                              // can't pass an argument to
                                              // action mode
                mode = v.startActionMode(actionModeCallback);
                wasDestroyedByWidget = true;
                ObjectAnimator.ofFloat(widgetDownButton, "alpha", 1f).start();
                ObjectAnimator.ofFloat(widgetUpButton, "alpha", 1f).start();
            } else {
                ClearHightlightedViews(false);
                currentHighlightedID = v.getId();
                highlightTargetView(v, "#FFFF66", R.drawable.hightlight_border,
                                true);

                mode = v.startActionMode(actionModeCallback);
                ObjectAnimator.ofFloat(widgetDownButton, "alpha", 1f).start();
                ObjectAnimator.ofFloat(widgetUpButton, "alpha", 1f).start();
            }

            // Handle relative layout utility panel
            if (v.getParent().getClass() == RelativeLayout.class
                            && !isRelativeUtilityOpen) {
                showRelativeUtilityPanel();
            } else if (v.getParent().getClass() != RelativeLayout.class
                            && isRelativeUtilityOpen) {
                hideRelativeUtilityPanel();
            }
        }
    }

    public class ContainerViewClickListener implements OnItemClickListener {
        // TODO fix highlighting containers
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                        long id) {
            performHightlight(parent);
        }
    }

    public class LayoutDragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                // what happens when the event starts
                if (event.getClipDescription().hasMimeType(
                                ClipDescription.MIMETYPE_TEXT_PLAIN)
                                && event.getClipDescription().getLabel()
                                                .equals("GridViewItem")) {
                    return (true);
                } else {
                    return (false);
                }
            case DragEvent.ACTION_DRAG_ENTERED:
                CallActionDescriptor();
                highlightTargetView(v, "#66FF00",
                                R.drawable.hightlight_border_green, false);
                tempHighlightedId = v.getId();

                actionDescriber.setText("Adding " + " on top of " + v.getTag());
                return (true);
            case DragEvent.ACTION_DRAG_LOCATION:
                return (true);
            case DragEvent.ACTION_DRAG_EXITED:
                DismissActionDescriptor();
                tempHighlightedId = 0;
                removeHighlight(v);
                return (true);
            case DragEvent.ACTION_DROP: {
                ClipData.Item item = event.getClipData().getItemAt(0);
                final String dragData = (String) item.getText();
                removeHighlight(v);
                // Toast.makeText(honeytest.this, "The Dragged data is: "
                // + dragData, Toast.LENGTH_LONG);

                createNewWidget(v, dragData, false, true, false, true);
                DismissActionDescriptor();

                if (currentHighlightedID == v.getId()) {
                    // means this layout was selected before the drag
                    highlightTargetView(v, "#FFFF66",
                                    R.drawable.hightlight_border, true);
                }
                if (isDragging) {
                    isDragging = false;
                    hideRadialMenu();
                }
                return (true);
            }
            case DragEvent.ACTION_DRAG_ENDED:
                if (isDragging) {
                    isDragging = false;
                    hideRadialMenu();
                }
                return (true);
            default:
                Toast.makeText(EditorActivity.this,
                                "Unknown action type received",
                                Toast.LENGTH_LONG).show();
                return (true);
            }
        }
    }

    public class DeleteDragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                // what happens when the event starts
                if (event.getClipDescription().hasMimeType(
                                ClipDescription.MIMETYPE_TEXT_PLAIN)
                                && event.getClipDescription().getLabel()
                                                .equals("TreeItem")) {
                    showDeleteBtn();
                    return (true);

                } else {
                    return (false);
                }
            case DragEvent.ACTION_DRAG_ENTERED:
                ObjectAnimator animX = ObjectAnimator.ofFloat(
                                widgetDeleteButton, "scaleX", 1.2f)
                                .setDuration(100);
                ObjectAnimator animY = ObjectAnimator.ofFloat(
                                widgetDeleteButton, "scaleY", 1.2f)
                                .setDuration(100);
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(animX, animY);
                animSet.start();
                return (true);
            case DragEvent.ACTION_DRAG_LOCATION:
                return (true);
            case DragEvent.ACTION_DRAG_EXITED:
                animX = ObjectAnimator
                                .ofFloat(widgetDeleteButton, "scaleX", 1f)
                                .setDuration(100);
                animY = ObjectAnimator
                                .ofFloat(widgetDeleteButton, "scaleY", 1f)
                                .setDuration(100);
                animSet = new AnimatorSet();
                animSet.playTogether(animX, animY);
                animSet.start();
                return (true);
            case DragEvent.ACTION_DROP: {
                View view = null;
                ClipData.Item item = event.getClipData().getItemAt(0);
                final String dragData = (String) item.getText();
                view = EditorActivity.this.findViewById(Integer
                                .valueOf(dragData));
                if (mode != null) {
                    mode.finish();
                }
                DeleteWidget(view);
                return (true);
            }
            case DragEvent.ACTION_DRAG_ENDED:
                hideDeleteBtn();
                animX = ObjectAnimator.ofFloat(widgetDeleteButton, "scaleX",
                                1.2f);
                animY = ObjectAnimator.ofFloat(widgetDeleteButton, "scaleY",
                                1.2f);
                animSet = new AnimatorSet();
                animSet.playTogether(animX, animY);
                animSet.start();
                return (true);
            default:
                Toast.makeText(EditorActivity.this,
                                "Unknown action type received",
                                Toast.LENGTH_LONG).show();
                return (false);
            }
        }
    }

    class WidgetAdapter extends ArrayAdapter<Widget> {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WidgetWrapper wrapper = null;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.row, null);
                wrapper = new WidgetWrapper(convertView);
                convertView.setTag(wrapper);
            } else {
                wrapper = (WidgetWrapper) convertView.getTag();
            }

            wrapper.populateFrom(getItem(position));
            return (convertView);
        }

        WidgetAdapter() {
            super(EditorActivity.this, R.layout.row, R.id.name, WidgetTypes.widgetsList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    public void hideDeleteBtn() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(widgetDeleteButton,
                        "alpha", 0f);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                widgetDeleteButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        anim.start();
    }

    public void showDeleteBtn() {
        AnimatorListener mListener = new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                widgetDeleteButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        };
        ObjectAnimator anim = ObjectAnimator.ofFloat(widgetDeleteButton,
                        "alpha", 1f);
        anim.addListener(mListener);
        anim.start();

    }

    public void showRelativeUtilityPanel() {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(
                        this, R.animator.slide_in_from_left);
        anim.setTarget(relativeUtilitiesLayout);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                isAnimationInProgress = true;
                relativeUtilitiesLayout.setVisibility(View.VISIBLE);
                mainlay.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isRelativeUtilityOpen = true;
                mainlay.setEnabled(true);
                isAnimationInProgress = false;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        if (!isRelativeUtilityOpen) {
            anim.start();
        }
    }

    public void hideRelativeUtilityPanel() {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(
                        this, R.animator.slide_out_to_left);
        anim.setTarget(relativeUtilitiesLayout);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                isAnimationInProgress = true;
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                relativeUtilitiesLayout.setVisibility(View.INVISIBLE);
                isRelativeUtilityOpen = false;
                isAnimationInProgress = false;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        if (isRelativeUtilityOpen) {
            anim.start();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenu.ContextMenuInfo menuInfo) {
        if (currentHighlightedID == 0 || currentHighlightedID != v.getId()) {
            v.performClick();
        }

        if (currentHighlightedID == v.getId()) {
            if (isLayout(v)) {
                new MenuInflater(this)
                                .inflate(R.menu.layout_context_menu, menu);
            } else {
                new MenuInflater(this)
                                .inflate(R.menu.widget_context_menu, menu);
            }
            if (v.getParent().getClass() != RelativeLayout.class) {
                menu.removeItem(R.id.context_layout_params);
            }
        }

        menu.removeItem(R.id.lock_context);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        viewHolder = null;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            showCloseSaveDialog();
            return (true);
        case R.id.exportBtn:
            exportXML();
            return (true);
        case R.id.saveBtn:
            saveActivityXML(true);
            return (true);
        case R.id.clear_menu_btn:
            ClearScreenDialog();
            return (true);
        case R.id.change_root_layout:
            ChangeRootLayoutDialog();
            return true;
        case R.id.newActivityBtn:
            AddNewActivityDialog();
            return (true);
        case R.id.manageActsBtn:
            startActivityManager();
            return true;
        case R.id.usage_guide_menu:
            openUserGuide();
            return true;
        case R.id.addResourceBtn:
            showFileOpenDialog();
            return true;
        case R.id.manageResBtn:
            openResourceManager();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openResourceManager() {
        saveActivityXML(false);
        Intent i = new Intent(this, ResourceManagerActivity.class);
        i.putExtra("project", projectInfo);
        startActivity(i);
    }

    private void changeWidgetBackground() {
        new ColorPickerDialog(this, new ColorSwitchedListener(), Color.BLACK)
                        .show();
    }

    class ColorSwitchedListener implements
                    ColorPickerDialog.OnColorChangedListener {
        @Override
        public void colorChanged(int color) {
            View view = findViewById(currentHighlightedID);
            if (view != null) {
                view.setBackgroundColor(color);
            }
        }
    }

    private void showFileOpenDialog() {
        String path = Environment.getExternalStorageDirectory() + "/";
        FileDialogFragment newFragment = FileDialogFragment.newInstance(path);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void addResource(String path) {
        File f = new File(path);
        File toPath = new File(Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + projectInfo.name + "/"
                        + "Images/" + f.getName());

        Toast.makeText(this,
                        "Added: " + f.getName() + " to the resource pool.",
                        Toast.LENGTH_SHORT).show();

        try {
            FileHelper.copy(f, toPath);
        } catch (IOException e) {
            // Log.d("file copy", "IS NOT WERK?");
            Toast.makeText(EditorActivity.this, "Error copying file!",
                            Toast.LENGTH_SHORT).show();
        }
    }

    private void openUserGuide() {
        saveActivityXML(true);
        Intent i = new Intent(this, UserGuideActivity.class);
        startActivity(i);
    }

    private void exportXML() {
        String path = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + projectInfo.name + "/"
                        + activityInfo.name + ".xml";

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here is my layout XML!");
        intent.putExtra(Intent.EXTRA_TEXT, "Project: " + projectInfo.name
                        + ". Activity: " + activityInfo.name);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));

        startActivity(Intent.createChooser(intent, "Share your layout XML!"));
    }

    private void showCloseSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to save first?")
                        .setCancelable(false)
                        .setTitle("Going back to the main screen...")
                        .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                saveActivityXML(true);
                                                finish();
                                            }
                                        })
                        .setNeutralButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                finish();
                                            }
                                        })
                        .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int which) {
                                                dialog.cancel();
                                            }
                                        }).show();
    }

    void showCloseSaveDialogAndChangeActivity(final int position) {
        // this is run from the spinner activity changer on the action bar
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to save first?")
                        .setCancelable(false)
                        .setTitle("Switching edited activity...")
                        .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                saveActivityXML(true);
                                                switchActivity(position);
                                            }
                                        })
                        .setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                switchActivity(position);
                                            }
                                        }).show();
    }

    private void startActivityManager() {
        saveActivityXML(true);
        Intent i = new Intent(this, ActivityManager.class);
        i.putExtra("project", projectInfo);
        startActivity(i);
    }

    private void refreshProjectData(boolean isNewIntent) {
        ProjectXMLParser parser = new ProjectXMLParser(projectInfo.name, this);
        parser.StartParser();
        projectInfo = parser.GetProjectInfo();
        activityInfo = parser.GetActivityInfo(projectInfo.mainActivityName);
        this.setTitle(projectInfo.name + " - ");

        if (isNewIntent) {
            LoadXML(projectInfo.name, activityInfo.name);
        }
        refreshEditorData(isNewIntent);

        setScreenSize(activityInfo.screenSize);
    }

    @Override
    protected void onStart() {
        super.onResume();
        // refreshProjectData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Grab info from the intent
        refreshProjectData(true);
        Bundle data = intent.getExtras();
        projectInfo = data.getParcelable("project");
        activityInfo = data.getParcelable("activity");
        this.setTitle(projectInfo.name + " - ");
        if (data.getBoolean("projectLoaderFlag")) {
            // if this activity is loaded from the project loader
            // we need to load the correct layout XML
            LoadXML(projectInfo.name, activityInfo.name);
        }
        if (data.getBoolean("isNewActivity")) {
            saveActivityXML(false);
        }

        // set the correct size
        setScreenSize(activityInfo.screenSize);

        ActionBar actionBar = getActionBar();
        int position = 0;
        for (int i = 0; i < activityList.size(); i++) {
            if (activityList.get(i).name.equals(activityInfo.name)) {
                position = i;
            }
        }
        actionBar.setSelectedNavigationItem(position);
    }

    private void AddNewActivityDialog() {
        final View sizeView = getLayoutInflater().inflate(
                        R.layout.new_activity_dialog, null);

        // set window size spinner values
        Spinner windowSize = (Spinner) sizeView.findViewById(R.id.new_act_size);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                        this, R.array.screenSizes,
                        android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        windowSize.setAdapter(adapter2);

        // set root layout types spinner
        final Spinner layoutType = (Spinner) sizeView
                        .findViewById(R.id.layout_types_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.rootLayoutTypes,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutType.setAdapter(adapter);

        new AlertDialog.Builder(this)
                        .setTitle("New Activity...")
                        .setView(sizeView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                EditText projName = (EditText) sizeView
                                                                .findViewById(R.id.new_act_name);
                                                Spinner activitySize = (Spinner) sizeView
                                                                .findViewById(R.id.new_act_size);

                                                boolean shouldProceed = true;
                                                if (!TextValidator
                                                                .isNameFieldValid(projName
                                                                                .getText()
                                                                                .toString())) {
                                                    Toast.makeText(EditorActivity.this,
                                                                    "Invalid activity name!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    dialog.cancel();
                                                    shouldProceed = false;
                                                }

                                                if (shouldProceed) {
                                                    String selectedLayoutType = layoutType
                                                                    .getSelectedItem()
                                                                    .toString();

                                                    ActivityInfo mainAct = new ActivityInfo();
                                                    mainAct.name = projName
                                                                    .getText()
                                                                    .toString();
                                                    mainAct.screenSize = activitySize
                                                                    .getSelectedItem()
                                                                    .toString();

                                                    AddNewActivity(mainAct,
                                                                    selectedLayoutType);
                                                }
                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    protected void AddNewActivity(ActivityInfo activity,
                    String selectedLayoutType) {
        saveActivityXML(true); // save the current activity
        ProjectXMLParser parser = new ProjectXMLParser(projectInfo.name, this);
        parser.StartParser();
        parser.AddNewActivityNode(activity);

        // make this new activity take over the editor
        activityInfo = activity;
        activityList.add(activity);
        ClearScreen(selectedLayoutType);
        EditorActivity.this.setTitle(projectInfo.name + " - ");

        mSpinnerAdapter.add((activity.name));
        mSpinnerAdapter.notifyDataSetChanged();

        for (int i = 0; i < activityList.size(); i++) {
            if (activityList.get(i).name.equals(activityInfo.name)) {
                getActionBar().setSelectedNavigationItem(i);
            }
        }
        setScreenSize(activityInfo.screenSize);
        saveActivityXML(false); // save the new empty activity
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (currentHighlightedID == 0) {
            Toast.makeText(this, "No widget selected!", Toast.LENGTH_SHORT)
                            .show();
            return (super.onContextItemSelected(item));
        } else {
            viewHolder = null;
            View v = (View) findViewById(currentHighlightedID);
            switch (item.getItemId()) {
            case R.id.delete_btn_context:
                showDeleteConfirmationDialog(v);
                return (true);
            case R.id.rename_btn_context:
                RenameWidget(v);
                return (true);
            case R.id.change_text_context:
                ChangeWidgetDisplay(v);
                return (true);
            case R.id.change_size_context:
                changeWidgetSizeDialog(v);
                return (true);
            case R.id.lock_context:
                lockWidget(v);
                return (true);
            case R.id.context_layout_params:
                ChangeWidgetLayoutParams(v, RetrieveLayoutStates(v));
                return (true);
            }
            return (super.onContextItemSelected(item));
        }
    }

    ActionMode.Callback actionModeCallback = new Callback() {
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (wasDestroyedByWidget) {
                ClearHightlightedViews(true);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            View v = (View) findViewById(currentHighlightedID);

            if (isLayout(v)) {
                getMenuInflater().inflate(R.menu.layout_action_menu, menu);

                // if (v.getClass() != TableLayout.class) {
                menu.removeItem(R.id.add_row);
                // }
            } else {
                getMenuInflater().inflate(R.menu.widget_action_menu, menu);
            }

            mode.setTitle(R.string.edit_mode_CAB);

            if (v.getClass() != LinearLayout.class) {
                menu.removeItem(R.id.menu_change_orientation);
            }

            if (v.getParent().getClass() != RelativeLayout.class) {
                menu.removeItem(R.id.menu_set_layout_parameters);
            }

            // TODO remove this when I figure out background colors
            menu.removeItem(R.id.menu_change_background);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (currentHighlightedID == 0) {
                Toast.makeText(EditorActivity.this, "No widget selected!",
                                Toast.LENGTH_SHORT).show();
                return (true);
            } else {
                View v = (View) findViewById(currentHighlightedID);
                switch (item.getItemId()) {
                case R.id.menu_delete:
                    showDeleteConfirmationDialog(v);
                    return (true);
                case R.id.menu_rename:
                    RenameWidget(v);
                    return (true);
                case R.id.menu_change_text:
                    ChangeWidgetDisplay(v);
                    return (true);
                case R.id.menu_fill_parent_h:
                    ToggleFillHeight(v);
                    return (true);
                case R.id.menu_fill_parent_w:
                    ToggleFillWidth(v);
                    return (true);
                case R.id.menu_change_size:
                    changeWidgetSizeDialog(v);
                    return (true);
                case R.id.menu_change_orientation:
                    toggleWidgetOrientation((LinearLayout) v);
                    return (true);
                case R.id.menu_set_layout_parameters:
                    ChangeWidgetLayoutParams(v, RetrieveLayoutStates(v));
                    return (true);
                case R.id.menu_change_margins:
                    setWidgetMarginsDialog(v);
                    return true;
                case R.id.menu_change_background:
                    changeWidgetBackground();
                    return true;
                case R.id.add_row:
                    addTableRow((TableLayout) v);
                    return true;
                }
            }
            return true;
        }
    };

    public View createNewWidget(View parent, String viewType,
                    boolean isCalledFromXmlLoader, boolean isEditable,
                    boolean isNewLayoutRoot, boolean shouldReport) {
        // event listeners
        OnClickListener mClickListener = new HightlightViewClickListener();
        OnDragListener mDragListen = new LayoutDragListener();

        View newWidget = null;
        String tag = "";

        if (viewType.equals(WidgetTypes.BUTTON_TAG)) {
            newWidget = createNewButton();
        } else if (viewType.equals(WidgetTypes.CHECKBOX_TAG)) {
            newWidget = createNewCheckbox();
        } else if (viewType.equals(WidgetTypes.TEXTVIEW_TAG)) {
            newWidget = createNewTextView();
        } else if (viewType.equals(WidgetTypes.IMAGEVIEW_TAG)) {
            newWidget = createNewImageView();
        } else if (viewType.equals(WidgetTypes.EDITTEXT_TAG)) {
            newWidget = createNewEditText();
        } else if (viewType.equals(WidgetTypes.RADIOBUTTON_TAG)) {
            newWidget = createNewRadioButton();
        } else if (viewType.equals(WidgetTypes.SPINNER_TAG)) {
            newWidget = createNewSpinner();
        } else if (viewType.equals(WidgetTypes.TOGGLEBUTTON_TAG)) {
            newWidget = createNewToggleButton();
        } else if (viewType.equals(WidgetTypes.LINEAR_TAG)) {
            newWidget = createNewLinearLayout(isCalledFromXmlLoader);
        } else if (viewType.equals(WidgetTypes.RELATIVE_TAG)) {
            newWidget = createNewRelativeLayout();
        } else if (viewType.equals(WidgetTypes.FRAME_TAG)) {
            newWidget = createNewFrameLayout();
        } else if (viewType.equals(WidgetTypes.TABLE_TAG)) {
            newWidget = createNewTableLayout();
        } else if (viewType.equals(WidgetTypes.LISTVIEW_TAG)) {
            newWidget = createNewListView();
        } else if (viewType.equals(WidgetTypes.RADIOGROUP_TAG)) {
            newWidget = createNewRadioGroup();
        } else if (viewType.equals(WidgetTypes.GRIDVIEW_TAG)) {
            newWidget = createNewGridView();
        } else if (viewType.equals(WidgetTypes.TABLEROW_TAG)) {
            newWidget = createNewTableRow();
        } else {
            Toast.makeText(this, "Unknown Widget Type", Toast.LENGTH_LONG)
                            .show();
        }

        if (newWidget != null) {
            // widget properties

            if (isEditable) {
                if (isListView(newWidget) || isSpinner(newWidget)
                                || newWidget.getClass() == GridView.class) {
                    // these will be selectable only from the tree
                } else {
                    newWidget.setOnClickListener(mClickListener);
                    registerForContextMenu(newWidget);
                }

            }

            // if its a layout, need to make it listen to drag events and be
            // same size as parent
            if (isLayout(newWidget)) {
                // if its a layout we generate a layout name
                newWidget.setOnDragListener(mDragListen);
                LayoutParams params = new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                newWidget.setLayoutParams(params);

                if (isNewLayoutRoot) { // if its a new LayoutRoot we have to
                    // name it properly
                    newWidget.setTag("LayoutRoot");
                } else {
                    tag = GenerateLayoutName();
                    newWidget.setTag(tag);
                }
            } else {
                tag = GenerateWidgetName();
                newWidget.setTag(tag);
            }

            int id = GenerateWidgetID();
            newWidget.setId(id);
            // add it to the view parent
            ((ViewGroup) parent).addView(newWidget);
            if (shouldReport) {
                // no need to report every widget if we create them from the XML
                // loader
                ReportWidgetAdded(newWidget.getClass().getSimpleName(), false,
                                tag);
            }
            if (parent.getClass() == RelativeLayout.class
                            && !parent.getTag().toString()
                                            .equals("MAIN_CANVAS")
                            && !isCalledFromXmlLoader) {
                // only call the relative layout dialog if its parent is a
                // relative layout
                // and its not called from the XML loader
                ChangeWidgetLayoutParams(newWidget,
                                RetrieveLayoutStates(newWidget));
            }
            // add it to the widget tree and refresh the TreeView
            if (parent.getTag().toString().equals("MAIN_CANVAS")) {
                manager.addAfterChild(null, (long) newWidget.getId(), null);
            } else {
                manager.addAfterChild((long) parent.getId(),
                                (long) newWidget.getId(), null);
            }

            simpleAdapter.refresh();

            return newWidget;
        }
        return null;
    }

    private View createNewTableRow() {
        TableRow newWidget = new TableRow(this);
        return newWidget;
    }

    protected void addTableRow(TableLayout v) {
        createNewWidget(v, WidgetTypes.TABLEROW_TAG, false, true, false, true);
    }

    private View createNewGridView() {
        GridView newWidget = new GridView(this);
        applyGridSampleData(newWidget);
        newWidget.setNumColumns(2);
        // newWidget.setStretchMode(GridView.AUTO_FIT);
        return newWidget;
    }

    private void applyGridSampleData(GridView newWidget) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.sampleData,
                        android.R.layout.simple_list_item_1);

        newWidget.setAdapter(adapter);
    }

    private View createNewRadioGroup() {
        RadioGroup newWidget = new RadioGroup(this);
        return newWidget;
    }

    private View createNewListView() {
        ListView newWidget = new ListView(this);
        applyListSampleData(newWidget);

        // ImageView newWidget = new ImageView(this);
        // newWidget.setImageResource(R.drawable.spinner);
        return newWidget;
    }

    private void applyListSampleData(ListView listView) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.sampleData,
                        android.R.layout.simple_list_item_1);

        listView.setAdapter(adapter);
    }

    private void applySpinnerSampleData(Spinner spinView) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.sampleData,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinView.setAdapter(adapter);
    }

    protected void toggleWidgetOrientation(LinearLayout v) {
        if (v.getOrientation() == LinearLayout.HORIZONTAL) {
            v.setOrientation(LinearLayout.VERTICAL);
        } else {
            v.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    private View createNewImageView() {
        ImageView newWidget = new ImageView(this);
        newWidget.setImageResource(R.drawable.icon);

        return newWidget;
    }

    private View createNewButton() {
        Button newWidget = new Button(this);
        newWidget.setText("Button");
        return newWidget;
    }

    private View createNewCheckbox() {

        CheckBox newWidget = new CheckBox(this);
        newWidget.setText("Checkbox");
        newWidget.setTextColor(Color.GRAY);
        newWidget.setActivated(false);
        return newWidget;
    }

    private View createNewTextView() {
        TextView newWidget = new TextView(this);
        newWidget.setText("TextView");
        newWidget.setTextColor(Color.GRAY);
        newWidget.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });
        return newWidget;
    }

    private View createNewEditText() {
        EditText newWidget = new EditText(this);
        newWidget.setText("EditText");
        newWidget.setTextColor(Color.GRAY);
        newWidget.setFocusableInTouchMode(false);
        return newWidget;
    }

    private View createNewRadioButton() {
        RadioButton newWidget = new RadioButton(this);
        newWidget.setText("RadioButton");
        newWidget.setTextColor(Color.GRAY);
        return newWidget;
    }

    private View createNewSpinner() {
        // ImageView newWidget = new ImageView(this);
        // newWidget.setImageResource(R.drawable.spinner);

        Spinner newWidget = new Spinner(this);
        applySpinnerSampleData(newWidget);
        return newWidget;
    }

    private View createNewToggleButton() {
        ToggleButton newWidget = new ToggleButton(this);
        registerForContextMenu(newWidget);
        return newWidget;
    }

    private View createNewLinearLayout(boolean isCalledFromXmlLoader) {
        final LinearLayout newlay = new LinearLayout(this);
        final CharSequence[] items = { "Vertical", "Horizontal" };

        if (!isCalledFromXmlLoader) {
            new AlertDialog.Builder(this)
                            .setTitle("Pick an Orientation for your LinearLayout...")
                            .setItems(items,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int item) {
                                                    if (item == 0) {
                                                        newlay.setOrientation(LinearLayout.VERTICAL);
                                                    } else {
                                                        newlay.setOrientation(LinearLayout.HORIZONTAL);
                                                    }
                                                }
                                            }).show();
        }
        return newlay;
    }

    private View createNewRelativeLayout() {
        RelativeLayout newlay = new RelativeLayout(this);
        return newlay;
    }

    private View createNewFrameLayout() {
        FrameLayout newlay = new FrameLayout(this);
        return newlay;
    }

    private View createNewTableLayout() {
        TableLayout newlay = new TableLayout(this);
        return newlay;
    }

    private void saveActivityXML(boolean shouldReport) {
        LayoutXMLCreator bla = new LayoutXMLCreator(activityInfo.name,
                        projectInfo.name);
        bla.StartLayoutNode(mainlay, true);

        IterateOverChildren(mainlay, bla);

        bla.FinishLayoutNode(mainlay);
        bla.endDocument();
        if (shouldReport) {
            Toast.makeText(this, "Activity saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void IterateOverChildren(ViewGroup vg, LayoutXMLCreator bla) {
        List<Long> temp = manager.getChildren((long) vg.getId());

        View v = null;
        Object[] children = temp.toArray();
        for (Object id : children) {
            v = findViewById(((Long) id).intValue());
            // if its a layout
            if (v.getClass().getSuperclass() == ViewGroup.class
                            || v.getClass() == TableLayout.class
                            || v.getClass() == RadioGroup.class) {
                bla.StartLayoutNode((ViewGroup) v, false);
                IterateOverChildren((ViewGroup) v, bla);
                bla.FinishLayoutNode((ViewGroup) v);
            } else { // if its a regular widget
                bla.AddWidgetNode(v, this);
            }
        }
    }

    private void LoadXML(String projectName, String activityName) {
        XmlPullParser parser = Xml.newPullParser();
        File newxmlfile;
        FileInputStream fileis = null;

        String path = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + projectName + "/"
                        + activityName + ".xml";

        try {
            resetNameGenerators(); // we are still checking for duplications
            newxmlfile = new File(path);
            fileis = new FileInputStream(newxmlfile);

            DeleteWidget(mainlay);

            parser.setInput(fileis, "utf-8");
            int eventType = parser.getEventType();

            Stack<ViewGroup> parentStack = new Stack<ViewGroup>();
            parentStack.push(UICanvas);

            while (eventType != XmlPullParser.END_DOCUMENT) {
                ArrayList<WidgetAttribute> attrList = new ArrayList<WidgetAttribute>();
                String tagName;
                View newWidget;
                boolean isNewRoot = false;

                if (eventType == XmlPullParser.START_TAG) {
                    // gather all the node attributes
                    for (int attrCount = 0; attrCount < parser
                                    .getAttributeCount(); attrCount++) {
                        String name = parser.getAttributeName(attrCount);
                        String value = parser.getAttributeValue(attrCount);
                        WidgetAttribute attribute = new WidgetAttribute(name,
                                        value);
                        attrList.add(attribute);

                        if (name.equals("id")
                                        && value.equals("@+id/LayoutRoot")) {
                            isNewRoot = true;
                        }
                    }

                    tagName = parser.getName();
                    if (isNewRoot) {
                        newWidget = createNewWidget(parentStack.peek(),
                                        tagName, true, false, isNewRoot, false);
                        mainlay = (ViewGroup) newWidget;
                    } else {
                        newWidget = createNewWidget(parentStack.peek(),
                                        tagName, true, true, isNewRoot, false);
                    }

                    applyWidgetAttributes(newWidget, attrList);
                    // if this is a layout it should hold the nodes coming after
                    // it
                    // as long as it wasn't closed of course
                    if (tagName.contains("Layout")
                                    || tagName.contains("RadioGroup")) {
                        parentStack.push((ViewGroup) newWidget);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().contains("Layout")) {
                        // if it ends a layout we should now add children to the
                        // last parent again
                        parentStack.pop();
                    }
                }

                eventType = parser.next();
            }
        } catch (Exception e) {
            // Log.e("Exception", "error occurred while parsing xml file");
            Toast.makeText(EditorActivity.this, "Error parsing XML file!",
                            Toast.LENGTH_SHORT).show();
        }
    }

    private void applyWidgetAttributes(View v,
                    ArrayList<WidgetAttribute> attrList) {
        ViewGroup.LayoutParams genericParams = v.getLayoutParams();
        RelativeLayout.LayoutParams relativeParams = null;

        if (v.getParent().getClass() == RelativeLayout.class) {
            relativeParams = (RelativeLayout.LayoutParams) genericParams;
        }
        int anchorID = 0;
        for (WidgetAttribute attr : attrList) {
            String name = attr.getAttrName();
            String value = attr.getAttrValue();
            if (name.equals("id")) {
                value = value.split("/")[1];
                v.setTag(value);
            } else if (name.equals("layout_height")) {
                if (value.toLowerCase(Locale.US).equals("match_parent")
                                || value.toLowerCase(Locale.US).equals(
                                                "fill_parent")) {
                    ToggleFillHeight(v);
                } else if (value.toLowerCase(Locale.US).equals("wrap_content")) {
                    genericParams.height = -2;
                } else {
                    genericParams.height = Integer
                                    .parseInt(value.split("dip")[0]);
                }
            } else if (name.equals("layout_width")) {
                if (value.toLowerCase(Locale.US).equals("match_parent")
                                || value.toLowerCase(Locale.US).equals(
                                                "fill_parent")) {
                    ToggleFillWidth(v);
                } else if (value.toLowerCase(Locale.US).equals("wrap_content")) {
                    genericParams.width = -2;
                } else {
                    genericParams.width = Integer
                                    .parseInt(value.split("dip")[0]);
                }
            } else if (name.equals("layout_marginTop")) {
                ((MarginLayoutParams) genericParams).topMargin = Integer
                                .parseInt(value.split("dip")[0]);
            } else if (name.equals("layout_marginRight")) {
                ((MarginLayoutParams) genericParams).rightMargin = Integer
                                .parseInt(value.split("dip")[0]);
            } else if (name.equals("layout_marginLeft")) {
                ((MarginLayoutParams) genericParams).leftMargin = Integer
                                .parseInt(value.split("dip")[0]);
            } else if (name.equals("layout_marginBottom")) {
                ((MarginLayoutParams) genericParams).bottomMargin = Integer
                                .parseInt(value.split("dip")[0]);
            } else if (name.equals("layout_margin")) {
                ((MarginLayoutParams) genericParams).topMargin = Integer
                                .parseInt(value.split("dip")[0]);
            } else if (name.equals("orientation")) {
                if (value.equals("vertical")) {
                    ((LinearLayout) v).setOrientation(LinearLayout.VERTICAL);
                } else {
                    ((LinearLayout) v).setOrientation(LinearLayout.HORIZONTAL);
                }
            } else if (name.equals("text")) {
                ((TextView) v).setText(value);
            } else if (name.equals("src")) {
                String fileName = value.substring(10);
                String[] availableImages = getAvailableImages();
                String path = null;
                for (int i = 0; i < availableImages.length; i++) {
                    if (availableImages[i].startsWith(fileName)) {
                        path = Environment.getExternalStorageDirectory()
                                        + "/Gui2Go/Projects/"
                                        + projectInfo.name + "/Images/"
                                        + availableImages[i];
                        fileName = availableImages[i];
                    }
                }
                if (path != null) {
                    Bitmap bmImg = BitmapFactory.decodeFile(path);
                    ((ImageView) v).setImageBitmap(bmImg);
                    // add it to the Map
                    imageDictionary.put(v.getId(), fileName);
                } else {
                    Toast.makeText(this, "Failed to load image...",
                                    Toast.LENGTH_LONG).show();
                    imageDictionary.remove(v.getId());
                }
            } else if (name.equals("layout_toLeftOf")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(0, anchorID);
            } else if (name.equals("layout_toRightOf")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(1, anchorID);
            } else if (name.equals("layout_above")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(2, anchorID);
            } else if (name.equals("layout_below")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(3, anchorID);
            } else if (name.equals("layout_alignBaseline")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(4, anchorID);
            } else if (name.equals("layout_alignLeft")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(5, anchorID);
            } else if (name.equals("layout_alignTop")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(6, anchorID);
            } else if (name.equals("layout_alignRight")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(7, anchorID);
            } else if (name.equals("layout_alignBottom")) {
                value = value.split("/")[1];
                anchorID = FindAnchorID(value, (ViewGroup) v.getParent());
                relativeParams.addRule(8, anchorID);
            } else if (name.equals("layout_alignParentLeft")) {
                relativeParams.addRule(9);
            } else if (name.equals("layout_alignParentTop")) {
                relativeParams.addRule(10);
            } else if (name.equals("layout_alignParentRight")) {
                relativeParams.addRule(11);
            } else if (name.equals("layout_alignParentBottom")) {
                relativeParams.addRule(12);
            } else if (name.equals("layout_centerInParent")) {
                relativeParams.addRule(13);
            } else if (name.equals("layout_centerHorizontal")) {
                relativeParams.addRule(14);
            } else if (name.equals("layout_centerVertical")) {
                relativeParams.addRule(15);
            }
        }
        // set layout params
        if (relativeParams != null) {
            v.setLayoutParams(relativeParams);
        } else {
            v.setLayoutParams(genericParams);
        }
    }

    private int FindAnchorID(String value, ViewGroup parent) {
        View anchor = null;
        // lets find the parent and anchor
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            if (v.getTag().toString().equals(value)) {
                anchor = v;
            }
        }

        if (anchor != null) {
            return anchor.getId();
        } else {
            Toast.makeText(EditorActivity.this, "Error.", Toast.LENGTH_SHORT)
                            .show();
            return 0;
        }
    }

    @SuppressWarnings("unused")
    private void ChangeWidgetWeight(final View v) {
        // CANT BE DONE IN CURRENT ANDROID API
        final View weightView = getLayoutInflater().inflate(
                        R.layout.set_weight_dialog, null);

        TextView currValue = (TextView) weightView
                        .findViewById(R.id.weight_dialog_current_val);
        currValue.setText((CharSequence) v);

        new AlertDialog.Builder(this)
                        .setTitle("Set weight...")
                        .setView(weightView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                EditText newname = (EditText) weightView
                                                                .findViewById(R.id.weight_dialog_new_val);
                                                CharSequence toSet = (CharSequence) newname
                                                                .getText();
                                                if (toSet.length() == 0) {
                                                    Toast.makeText(EditorActivity.this,
                                                                    "Cannot set empty value! Made no changes.",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                } else {
                                                    ((TextView) v).setText(toSet);
                                                    mode.finish();
                                                }

                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    @SuppressWarnings("unused")
    private void ChangeWidgetGravity(final View v) {
        final CharSequence[] items = { "Center", "Bottom", "Center horizontal",
                        "Center vertical", "Left", "Right", "Top" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose gravity value:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {

                }
            }
        }).show();
    }

    private void ChangeWidgetLayoutParams(final View v, boolean[] states) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose gravity value:");
        builder.setMultiChoiceItems(R.array.relativeLayoutValues, states,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int item,
                                            boolean arg2) {
                                ToggleLayoutParam(v, item);
                            }
                        }).show();
    }

    private void ToggleLayoutParam(final View v, final int position) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v
                        .getLayoutParams();
        ArrayList<CharSequence> list = getRelevantAnchors(v);
        int[] rules;
        rules = params.getRules();
        int size = list.size();
        final CharSequence[] temp = new CharSequence[size];
        list.toArray(temp);
        viewHolder = v;
        relativePosition = position;

        if (position >= 0 && position <= 8) // needs a target
        {
            if (rules[position] == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Pick an anchor");
                builder.setItems(temp, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        int targetID = 0;
                        CharSequence targetName = temp[item];
                        ViewGroup parent = (ViewGroup) viewHolder.getParent();
                        targetID = FindAnchorID(targetName.toString(), parent);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder
                                        .getLayoutParams();
                        params.addRule(relativePosition, targetID);

                        // refresh the view
                        viewHolder = null;

                        v.requestLayout();
                    }
                }).show();
            } else {
                params.addRule(position, 0);
                v.requestLayout();
            }
        } else {
            if (rules[position] != -1) {
                params.addRule(position);
                v.requestLayout();
            } else {
                params.addRule(position, 0);
                v.requestLayout();
            }
        }
    }

    private ArrayList<CharSequence> getRelevantAnchors(View v) {
        ArrayList<CharSequence> result = new ArrayList<CharSequence>();
        ViewGroup parent = (ViewGroup) v.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            result.add((CharSequence) parent.getChildAt(i).getTag());
        }
        result.remove((CharSequence) v.getTag());
        return result;
    }

    private boolean[] RetrieveLayoutStates(View v) {
        boolean[] states = { false, false, false, false, false, false, false,
                        false, false, false, false, false, false, false, false,
                        false, };
        int[] rules;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v
                        .getLayoutParams();

        rules = params.getRules();

        if (rules[0] != 0) {
            states[0] = true;
        }
        if (rules[1] != 0) {
            states[1] = true;
        }
        if (rules[2] != 0) {
            states[2] = true;
        }
        if (rules[3] != 0) {
            states[3] = true;
        }
        if (rules[4] != 0) {
            states[4] = true;
        }
        if (rules[5] != 0) {
            states[5] = true;
        }
        if (rules[6] != 0) {
            states[6] = true;
        }
        if (rules[7] != 0) {
            states[7] = true;
        }
        if (rules[8] != 0) {
            states[8] = true;
        }
        if (rules[9] == -1) {
            states[9] = true;
        }
        if (rules[10] == -1) {
            states[10] = true;
        }
        if (rules[11] == -1) {
            states[11] = true;
        }
        if (rules[12] == -1) {
            states[12] = true;
        }
        if (rules[13] != 0) {
            states[13] = true;
        }
        if (rules[14] != 0) {
            states[14] = true;
        }
        if (rules[15] != 0) {
            states[15] = true;
        }

        return states;
    }

    private void lockWidget(View v) {
        // for (int i = 0; i < treeView.getCount(); i++) { // highlight the
        // // tree too
        // View viewFromTree = (View) treeView.getChildAt(i);
        // if (viewFromTree != null) {
        // String treeViewTag = viewFromTree.getTag().toString();
        //
        // if (treeViewTag.equals(String.valueOf(v.getId()))) {
        // viewFromTree.findViewById(id)
        // simpleAdapter.setHighlightedID(viewFromTree);
        // }
        // }
        // }
    }

    protected void setWidgetMarginsDialog(final View v) {
        final View sizeView = getLayoutInflater().inflate(
                        R.layout.set_widget_margins_dialog, null);

        EditText topMarginTxt = (EditText) sizeView
                        .findViewById(R.id.topMarginEditText);
        EditText leftMarginTxt = (EditText) sizeView
                        .findViewById(R.id.leftMarginEditText);
        EditText rightMarginTxt = (EditText) sizeView
                        .findViewById(R.id.rightMarginEditText);
        EditText bottomMarginTxt = (EditText) sizeView
                        .findViewById(R.id.bottomMarginEditText);

        ViewGroup.MarginLayoutParams genericParams = (MarginLayoutParams) v
                        .getLayoutParams();
        topMarginTxt.setText(String.valueOf(genericParams.topMargin));
        leftMarginTxt.setText(String.valueOf(genericParams.leftMargin));
        rightMarginTxt.setText(String.valueOf(genericParams.rightMargin));
        bottomMarginTxt.setText(String.valueOf(genericParams.bottomMargin));

        new AlertDialog.Builder(this)
                        .setTitle("Set widget margins: (Use dip values without the dip text)")
                        .setView(sizeView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                EditText topMarginTxt = (EditText) sizeView
                                                                .findViewById(R.id.topMarginEditText);
                                                EditText leftMarginTxt = (EditText) sizeView
                                                                .findViewById(R.id.leftMarginEditText);
                                                EditText rightMarginTxt = (EditText) sizeView
                                                                .findViewById(R.id.rightMarginEditText);
                                                EditText bottomMarginTxt = (EditText) sizeView
                                                                .findViewById(R.id.bottomMarginEditText);

                                                String topMargin = topMarginTxt
                                                                .getText()
                                                                .toString();
                                                String leftMargin = leftMarginTxt
                                                                .getText()
                                                                .toString();
                                                String rightMargin = rightMarginTxt
                                                                .getText()
                                                                .toString();
                                                String bottomMargin = bottomMarginTxt
                                                                .getText()
                                                                .toString();

                                                setWidgetMargins(v, topMargin,
                                                                leftMargin,
                                                                bottomMargin,
                                                                rightMargin);

                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    private void setWidgetMargins(View v, String top, String left,
                    String bottom, String right) {
        ViewGroup.LayoutParams genericParams = v.getLayoutParams();
        RelativeLayout.LayoutParams relativeParams = null;

        if (v.getParent().getClass() == RelativeLayout.class) {
            relativeParams = (RelativeLayout.LayoutParams) genericParams;
        }

        if (!top.isEmpty()) {
            int topPixel = convertDipToPixel(Float.parseFloat(top));
            ((MarginLayoutParams) genericParams).topMargin = topPixel;
        }
        if (!left.isEmpty()) {
            int leftPixel = convertDipToPixel(Float.parseFloat(left));
            ((MarginLayoutParams) genericParams).leftMargin = leftPixel;
        }
        if (!bottom.isEmpty()) {
            int bottomPixel = convertDipToPixel(Float.parseFloat(bottom));
            ((MarginLayoutParams) genericParams).bottomMargin = bottomPixel;
        }
        if (!right.isEmpty()) {
            int rightPixel = convertDipToPixel(Float.parseFloat(right));
            ((MarginLayoutParams) genericParams).rightMargin = rightPixel;
        }

        if (relativeParams != null) {
            v.setLayoutParams(relativeParams);
        } else {
            v.setLayoutParams(genericParams);
        }
    }

    protected void changeWidgetSizeDialog(final View v) {
        final View sizeView = getLayoutInflater().inflate(
                        R.layout.change_size_dialog, null);

        TextView currWidth = (TextView) sizeView
                        .findViewById(R.id.size_dialog_curr_w);
        TextView currHeight = (TextView) sizeView
                        .findViewById(R.id.size_dialog_curr_h);
        currWidth.setText(String.valueOf(v.getWidth()));
        currHeight.setText(String.valueOf(v.getHeight()));

        new AlertDialog.Builder(this)
                        .setTitle("Rename widget... (0 means wrap_content)")
                        .setView(sizeView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                int toSetH;
                                                int toSetW;

                                                EditText newWidth = (EditText) sizeView
                                                                .findViewById(R.id.size_dialog_w);
                                                EditText newHeight = (EditText) sizeView
                                                                .findViewById(R.id.size_dialog_h);
                                                String widthStr = newWidth
                                                                .getText()
                                                                .toString();
                                                String heightStr = newHeight
                                                                .getText()
                                                                .toString();
                                                if (widthStr.length() == 0
                                                                || heightStr.length() == 0) {
                                                    Toast.makeText(EditorActivity.this,
                                                                    "Cannot set empty property!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                    dialog.cancel();
                                                } else {
                                                    try {
                                                        toSetH = Integer.parseInt(heightStr);
                                                        toSetW = Integer.parseInt(widthStr);
                                                        if (toSetH < maxHeight
                                                                        || toSetW < maxWidth) {
                                                            ChangeWidgetSize(
                                                                            v,
                                                                            toSetH,
                                                                            toSetW);
                                                        } else {
                                                            Toast.makeText(EditorActivity.this,
                                                                            "Cannot make widget bigger than the screen!",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();
                                                        }
                                                    } catch (Exception e) {
                                                        Toast.makeText(EditorActivity.this,
                                                                        "Invalid input",
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                    }
                                                }

                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    private void ChangeWidgetSize(View v, int toSetH, int toSetW) {
        // LayoutParams params = null;
        ViewGroup.LayoutParams params = v.getLayoutParams();

        if (v.getParent().getClass() == LinearLayout.class) {
            params = (LinearLayout.LayoutParams) params;
        } else if (v.getParent().getClass() == FrameLayout.class) {
            params = (FrameLayout.LayoutParams) params;
        } else if (v.getParent().getClass() == RelativeLayout.class) {
            params = (RelativeLayout.LayoutParams) params;
        } else if (v.getParent().getClass() == TableLayout.class) {
            params = (TableLayout.LayoutParams) params;
        } else if (v.getParent().getClass() == RadioGroup.class) {
            params = (RadioGroup.LayoutParams) params;
        }

        if (toSetW == 0 && toSetH == 0) // both are wrap content
        {
            params.height = LayoutParams.WRAP_CONTENT;
            params.width = LayoutParams.WRAP_CONTENT;
        } else if (toSetW != 0 && toSetH == 0) {
            // only H is wrap
            params.width = toSetW;
            params.height = LayoutParams.WRAP_CONTENT;
        } else if (toSetW == 0 && toSetH != 0) {
            // only W is wrap
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = toSetH;
        } else {
            params.width = toSetW;
            params.height = toSetH;
        }
        v.setLayoutParams(params);
        v.invalidate();
        mode.finish();
    }

    protected void ToggleFillWidth(View v) {
        int oldHeight = v.getHeight();
        LayoutParams params = null;
        if (v.getLayoutParams().width != -1) {
            if (v.getParent().getClass() == LinearLayout.class) {
                params = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, oldHeight);
            } else if (v.getParent().getClass() == FrameLayout.class) {
                params = new FrameLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, oldHeight);
            } else if (v.getParent().getClass() == RelativeLayout.class) {
                params = new RelativeLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, oldHeight);
            } else if (v.getParent().getClass() == TableLayout.class) {
                params = new TableLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, oldHeight);
            } else if (v.getParent().getClass() == RadioGroup.class) {
                params = new RadioGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                                oldHeight);
            }
        } else {
            if (v.getParent().getClass() == LinearLayout.class) {
                params = new LinearLayout.LayoutParams(
                                LayoutParams.WRAP_CONTENT, oldHeight);
            } else if (v.getParent().getClass() == FrameLayout.class) {
                params = new FrameLayout.LayoutParams(
                                LayoutParams.WRAP_CONTENT, oldHeight);
            } else if (v.getParent().getClass() == RelativeLayout.class) {
                params = new RelativeLayout.LayoutParams(
                                LayoutParams.WRAP_CONTENT, oldHeight);
            } else if (v.getParent().getClass() == TableLayout.class) {
                params = new TableLayout.LayoutParams(
                                LayoutParams.WRAP_CONTENT, oldHeight);
            } else if (v.getParent().getClass() == RadioGroup.class) {
                params = new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                                oldHeight);
            }
        }
        v.setLayoutParams(params);
        v.invalidate();
    }

    protected void ToggleFillHeight(View v) {
        int oldWidth = v.getWidth();
        LayoutParams params = null;
        if (v.getLayoutParams().height != -1) {
            if (v.getParent().getClass() == LinearLayout.class) {
                params = new LinearLayout.LayoutParams(oldWidth,
                                LayoutParams.MATCH_PARENT);
            } else if (v.getParent().getClass() == FrameLayout.class) {
                params = new FrameLayout.LayoutParams(oldWidth,
                                LayoutParams.MATCH_PARENT);
            } else if (v.getParent().getClass() == RelativeLayout.class) {
                params = new RelativeLayout.LayoutParams(oldWidth,
                                LayoutParams.MATCH_PARENT);
            } else if (v.getParent().getClass() == TableLayout.class) {
                params = new TableLayout.LayoutParams(oldWidth,
                                LayoutParams.MATCH_PARENT);
            } else if (v.getParent().getClass() == RadioGroup.class) {
                params = new RadioGroup.LayoutParams(oldWidth,
                                LayoutParams.MATCH_PARENT);
            }
        } else {
            if (v.getParent().getClass() == LinearLayout.class) {
                params = new LinearLayout.LayoutParams(oldWidth,
                                LayoutParams.WRAP_CONTENT);
            } else if (v.getParent().getClass() == FrameLayout.class) {
                params = new FrameLayout.LayoutParams(oldWidth,
                                LayoutParams.WRAP_CONTENT);
            } else if (v.getParent().getClass() == RelativeLayout.class) {
                params = new RelativeLayout.LayoutParams(oldWidth,
                                LayoutParams.WRAP_CONTENT);
            } else if (v.getParent().getClass() == TableLayout.class) {
                params = new TableLayout.LayoutParams(oldWidth,
                                LayoutParams.WRAP_CONTENT);
            } else if (v.getParent().getClass() == RadioGroup.class) {
                params = new RadioGroup.LayoutParams(oldWidth,
                                LayoutParams.WRAP_CONTENT);
            }
        }
        v.setLayoutParams(params);
        v.invalidate();
    }

    private void ChangeWidgetDisplay(View v) {
        if ((v.getClass() == ImageView.class)
                        || v.getClass() == ImageButton.class) {
            ChangeWidgetImage(v);
        } else {
            ChangeWidgetText(v);
        }
    }

    private void ChangeWidgetImage(final View v) {
        final String[] availableImages = getAvailableImages();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick an anchor");
        builder.setItems(availableImages,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                            int position) {
                                String fileName = availableImages[position];
                                String path = Environment
                                                .getExternalStorageDirectory()
                                                + "/Gui2Go/Projects/"
                                                + projectInfo.name
                                                + "/Images/"
                                                + fileName;

                                Bitmap bmImg = BitmapFactory.decodeFile(path);
                                ((ImageView) v).setImageBitmap(bmImg);
                                imageDictionary.put(v.getId(), fileName);
                            }
                        }).show();
    }

    private String[] getAvailableImages() {
        String path = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + projectInfo.name + "/Images/";

        File f = new File(path);
        return f.list();
    }

    private void ChangeWidgetText(final View v) {
        final View renameView = getLayoutInflater().inflate(
                        R.layout.rename_dialog, null);
        TextView currName = (TextView) renameView
                        .findViewById(R.id.rename_dialog_curr_name);
        currName.setText((CharSequence) ((TextView) v).getText());

        new AlertDialog.Builder(this)
                        .setTitle("Set Text...")
                        .setView(renameView)
                        .setPositiveButton("OK",
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
                                                    Toast.makeText(EditorActivity.this,
                                                                    "Cannot set empty name! Made no changes.",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                } else {
                                                    ((TextView) v).setText(toSet);
                                                    mode.finish();
                                                }

                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    private void RenameWidget(final View v) {
        final View renameView = getLayoutInflater().inflate(
                        R.layout.rename_dialog, null);

        final TextView currName = (TextView) renameView
                        .findViewById(R.id.rename_dialog_curr_name);
        currName.setText((CharSequence) v.getTag());

        new AlertDialog.Builder(this)
                        .setTitle("Rename widget...")
                        .setView(renameView)
                        .setPositiveButton("OK",
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
                                                    Toast.makeText(EditorActivity.this,
                                                                    "Cannot set empty name! Made no changes.",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                } else if (widgetNames
                                                                .contains(toSet)) {
                                                    Toast.makeText(EditorActivity.this,
                                                                    "Name already exists!",
                                                                    Toast.LENGTH_SHORT)
                                                                    .show();
                                                } else {
                                                    widgetNames.remove(v
                                                                    .getTag());
                                                    v.setTag(toSet);
                                                    widgetNames.add(toSet
                                                                    .toString());
                                                    widgetNames.remove(currName);
                                                    simpleAdapter.refresh();
                                                    mode.finish();
                                                }
                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    public void ClearHightlightedViews(boolean isCalledFromActionMode) {
        if (currentHighlightedID != 0) {
            View v = (View) findViewById(currentHighlightedID);
            if (isLayout(v) || isListView(v) || isTextualView(v)
                            || v.getClass() == GridView.class) {
                v.setBackgroundDrawable(null);
            } else if (v.getClass() == ImageView.class) {
                ((ImageView) v).setColorFilter(null);
            } else {
                v.getBackground().setColorFilter(null);
            }
            currentHighlightedID = 0;
            v.invalidate();
            hideRelativeUtilityPanel();
            if (!isCalledFromActionMode) {
                mode.finish();
            }
        }

        View highlightedTreeNode = simpleAdapter.getHighlightedID(); // clear
        // highlight
        // in
        // the
        // treeView
        if (highlightedTreeNode != null) {
            ((ViewGroup) highlightedTreeNode).setBackgroundDrawable(null);
            simpleAdapter.setHighlightedID(null);
        }
    }

    public void DeleteWidget(View v) {
        if (mode != null) {
            mode.finish();
        }
        manager.removeNodeRecursively((long) v.getId());
        widgetIDs.remove((Integer) v.getId());
        widgetNames.remove(v.getTag());
        ViewGroup parent = (ViewGroup) v.getParent();
        parent.removeView(v);
        currentHighlightedID = 0; // clear selection
    }

    private void showDeleteConfirmationDialog(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want delete this widget?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                DeleteWidget(v);
                                            }
                                        })
                        .setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                dialog.cancel();
                                            }
                                        }).show();
    }

    private int convertDipToPixel(float dip) {
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dip, r.getDisplayMetrics());
        return (px);
    }

    // private float convertPixelToDip(int pixel)
    // {
    // float scale = getResources().getDisplayMetrics().density;
    // float dips = pixel / scale;
    // return dips;
    // }

    private void ReportWidgetAdded(String widgetType, boolean isFillParent,
                    String tag) {
        if (isFillParent == true) {
            Toast.makeText(this,
                            "Added "
                                            + widgetType
                                            + ", filling parent by default. Tag is "
                                            + tag, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                            "Added " + widgetType + ", default size. Tag is "
                                            + tag, Toast.LENGTH_SHORT).show();
        }
    }

    private String GenerateWidgetName() {
        lastWidgetDisplayID++;
        String res = "widget" + String.valueOf(lastWidgetDisplayID);
        while (widgetNames.contains(res)) {
            lastWidgetDisplayID++;
            res = "widget" + String.valueOf(lastWidgetDisplayID);
        }
        widgetNames.add(res);
        return (res);
    }

    private String GenerateLayoutName() {
        lastLayoutDisplayID++;
        String res = "layout" + String.valueOf(lastLayoutDisplayID);
        while (widgetNames.contains(res)) {
            lastLayoutDisplayID++;
            res = "layout" + String.valueOf(lastLayoutDisplayID);
        }
        widgetNames.add(res);
        return (res);
    }

    private int GenerateWidgetID() {
        lastWidgetID++;
        int res = lastWidgetID;
        while (widgetIDs.contains(res)) {
            res++;
        }
        widgetIDs.add(res);
        return (res);
    }

    public void toolboxBtnClick(View v) {
        // logic should be like this
        // if nothing is open, open what you clicked on
        // if what you clicked on is open, close the panel
        // if you clicked on the other button, switch to that

        if (!isToolboxOpen && !isTreeOpen) {
            if (!wasToolboxLastOpen) {
                flipper.showNext();
            }
            isToolboxOpen = true;
            showToolboxPanel();
            ObjectAnimator.ofFloat(v.getParent(), "x", 960f).start();
            ((ImageButton) v).setImageResource(R.drawable.ic_tools_on1_1);
        } else if (isToolboxOpen) {
            hideToolboxPanel();
            ObjectAnimator.ofFloat(v.getParent(), "x", 1210f).start();
            ((ImageButton) v).setImageResource(R.drawable.ic_tools1_1);
            isToolboxOpen = false;
            wasToolboxLastOpen = true;
        } else if (isTreeOpen) {
            flipper.showNext();
            treeButton.setImageResource(R.drawable.ic_tree1_1);
            ((ImageButton) v).setImageResource(R.drawable.ic_tools_on1_1);
            isToolboxOpen = true;
            isTreeOpen = false;
        }

        if (widgetAddButton.getAlpha() == 1f) {
            // if the add buttons happens to be showing
            ObjectAnimator.ofFloat(widgetAddButton, "alpha", 0f).start();
        }
    }

    private void showToolboxPanel() {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(
                        this, R.animator.slide_in_from_right);
        anim.setTarget(toolboxLinearLayout);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                toolboxLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        anim.start();
    }

    private void hideToolboxPanel() {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(
                        this, R.animator.slide_out_to_right);
        anim.setTarget(toolboxLinearLayout);
        anim.start();
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                toolboxLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
    }

    public void treeBtnClick(View v) {
        if (!isToolboxOpen && !isTreeOpen) {
            if (wasToolboxLastOpen) {
                flipper.showNext();
            }
            isTreeOpen = true;
            showToolboxPanel();
            ObjectAnimator.ofFloat(v.getParent(), "x", 960f).start();
            ((ImageButton) v).setImageResource(R.drawable.ic_tree_on1_1);
        } else if (isTreeOpen) {
            hideToolboxPanel();
            ObjectAnimator.ofFloat(v.getParent(), "x", 1210f).start();
            ((ImageButton) v).setImageResource(R.drawable.ic_tree1_1);
            isTreeOpen = false;
            wasToolboxLastOpen = false;
        } else if (isToolboxOpen) {
            flipper.showNext();
            isTreeOpen = true;
            isToolboxOpen = false;
            toolboxButton.setImageResource(R.drawable.ic_tools1_1);
            ((ImageButton) v).setImageResource(R.drawable.ic_tree_on1_1);
        }

        if (widgetAddButton.getAlpha() == 1f) {
            // if the add buttons happens to be showing
            ObjectAnimator.ofFloat(widgetAddButton, "alpha", 0f).start();
        }
    }

    public void DismissActionDescriptor() {
        ObjectAnimator.ofFloat(actionDescriber, "alpha", 0f).start();
    }

    public void CallActionDescriptor() {
        ObjectAnimator.ofFloat(actionDescriber, "alpha", 1f).start();
    }

    protected void resetNameGenerators() {
        widgetNames.clear();
        widgetIDs.clear();
        lastWidgetID = 1000;
        lastWidgetDisplayID = 0;
        lastLayoutDisplayID = 0;
    }

    private void ClearScreenDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to clear this activity?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                newClearScreen();
                                            }
                                        })
                        .setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                dialog.cancel();
                                            }
                                        }).show();
    }

    protected void newClearScreen() {
        resetNameGenerators();
        int count = mainlay.getChildCount();
        for (int i = 0; i < count; i++) {
            DeleteWidget(mainlay.getChildAt(0));
        }
    }

    private void ChangeRootLayoutDialog() {
        final View sizeView = getLayoutInflater().inflate(
                        R.layout.change_root_layout_dialog, null);

        // set root layout types spinner
        final Spinner layoutType = (Spinner) sizeView
                        .findViewById(R.id.layout_types_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.rootLayoutTypes,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutType.setAdapter(adapter);

        TextView currentType = (TextView) sizeView
                        .findViewById(R.id.current_layout_type);
        currentType.setText(mainlay.getClass().getSimpleName().toString());

        new AlertDialog.Builder(this)
                        .setTitle("Change root layout type...")
                        .setView(sizeView)
                        .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int whichbutton) {
                                                String selectedLayoutType = layoutType
                                                                .getSelectedItem()
                                                                .toString();

                                                ClearScreen(selectedLayoutType);
                                            }
                                        }).setNegativeButton("Cancel", null)
                        .show();
    }

    protected void ClearScreen(String selectedLayoutType) {
        DeleteWidget(mainlay);
        // reset all generators
        resetNameGenerators();

        ViewGroup newWidget;
        if (selectedLayoutType == null) {
            newWidget = (ViewGroup) createNewWidget(UICanvas, "LinearLayout",
                            false, false, true, false);
        } else {
            newWidget = (ViewGroup) createNewWidget(UICanvas,
                            selectedLayoutType, false, false, true, false);
        }

        mainlay = (ViewGroup) newWidget;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showCloseSaveDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void removeHighlight(View v) {
        if (v != null) {
            if (isLayout(v) || isListView(v) || isTextualView(v)
                            || v.getClass() == GridView.class) {
                v.setBackgroundDrawable(null);
            } else if (v.getClass() == ImageView.class) {
                ((ImageView) v).setColorFilter(null);
            } else {
                v.getBackground().setColorFilter(null);
            }

            View highlightedTreeNode = simpleAdapter.getHighlightedID(); // clear
            // highlight
            // in
            // the
            // treeView
            if (highlightedTreeNode != null) {
                ((ViewGroup) highlightedTreeNode).setBackgroundDrawable(null);
                simpleAdapter.setHighlightedID(null);
            }
            v.invalidate();
        }
    }

    private void highlightTargetView(View v, String rgbValue, int borderId,
                    boolean highlightInTree) {
        if (tempHighlightedId != v.getId() && tempHighlightedId != 0) {
            removeHighlight(findViewById(tempHighlightedId));
        }
        if (isLayout(v) || isListView(v) || isTextualView(v)
                        || v.getClass() == GridView.class) {
            v.setBackgroundResource(borderId);
        } else if (v.getClass() == ImageView.class) {
            ((ImageView) v).setColorFilter(Color.parseColor(rgbValue),
                            Mode.DARKEN);
        } else {
            v.getBackground().setColorFilter(Color.parseColor(rgbValue),
                            Mode.DARKEN);
        }

        if (highlightInTree) {
            for (int i = 0; i < treeView.getCount(); i++) { // highlight the
                // tree too
                View viewFromTree = (View) treeView.getChildAt(i);
                if (viewFromTree != null) {
                    String treeViewTag = viewFromTree.getTag().toString();

                    if (treeViewTag.equals(String.valueOf(v.getId()))) {
                        viewFromTree.setBackgroundResource(borderId);
                        simpleAdapter.setHighlightedID(viewFromTree);
                    }
                }
            }
        }
    }

    private boolean isTextualView(View v) {
        if (v.getClass() == TextView.class || v.getClass() == CheckBox.class
                        || v.getClass() == RadioButton.class) {
            return true;
        }
        return false;
    }

    public boolean isLayout(View v) {
        if (v.getClass().getSuperclass() == ViewGroup.class
                        || v.getClass() == TableLayout.class
                        || v instanceof LinearLayout
                        || v.getClass() == TableRow.class) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isListView(View v) {
        if (v.getClass() == ListView.class) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSpinner(View v) {
        if (v.getClass() == Spinner.class) {
            return true;
        } else {
            return false;
        }
    }

    public void InsertViewToParent(View v, ViewGroup target) {
        // add it to the new parent
        target.addView(v);
        // add it to the widget tree and refresh the TreeView
        manager.addAfterChild((long) target.getId(), (long) v.getId(), null);
        if (isLayout(v)) {
            ViewGroup temp = (ViewGroup) v;
            View child = null;

            int childCount = temp.getChildCount();
            for (int i = 0; i < childCount; i++) {
                child = temp.getChildAt(i);
                manager.addAfterChild((long) v.getId(), (long) child.getId(),
                                null);
            }
        }
        simpleAdapter.refresh();
    }

    public void insertViewToTreeParentAfterTarget(View v, ViewGroup parent,
                    View target) {
        // add it to the widget tree and refresh the TreeView
        if (target == null) {
            manager.addAfterChild((long) parent.getId(), (long) v.getId(), null);
        } else {
            manager.addBeforeChild((long) parent.getId(), (long) v.getId(),
                            (long) target.getId());
        }
        simpleAdapter.refresh();
    }

    // private void toggleScaleHighlight(View v)
    // {
    // if (v.getScaleX() != 1.3f && v.getScaleY() != 1.3f) {
    // // turn on highlight
    // ObjectAnimator animX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f);
    // ObjectAnimator animY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f);
    // AnimatorSet animSet = new AnimatorSet();
    // animSet.playTogether(animX, animY);
    // animSet.start();
    // } else {
    // ObjectAnimator animX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f);
    // ObjectAnimator animY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f);
    // AnimatorSet animSet = new AnimatorSet();
    // animSet.playTogether(animX, animY);
    // animSet.start();
    // }
    // }

    private void toggleAddButton() {
        if (currentSelectedGridItemId != -1) {
            ObjectAnimator.ofFloat(widgetAddButton, "alpha", 1f).start();
        } else {
            ObjectAnimator.ofFloat(widgetAddButton, "alpha", 0f).start();
        }
    }

    private void switchActivity(final int position) {
        activityInfo = activityList.get(position);
        LoadXML(projectInfo.name, activityInfo.name);
        EditorActivity.this.setTitle(projectInfo.name + " - ");
        setScreenSize(activityInfo.screenSize);
    }
}