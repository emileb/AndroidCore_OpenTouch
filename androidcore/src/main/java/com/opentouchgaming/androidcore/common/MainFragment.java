package com.opentouchgaming.androidcore.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.opentouchgaming.androidcore.AboutDialog;
import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.GamepadActivity;
import com.opentouchgaming.androidcore.ItemClickSupport;
import com.opentouchgaming.androidcore.LogViewDialog;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.ScopedStorage;
import com.opentouchgaming.androidcore.SubGame;
import com.opentouchgaming.androidcore.SubGameRecyclerViewAdapter;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.androidcore.controls.Dpad;
import com.opentouchgaming.androidcore.license.LicenseCheck;
import com.opentouchgaming.androidcore.ui.EnginesPanel;
import com.opentouchgaming.androidcore.ui.OptionsDialog;
import com.opentouchgaming.androidcore.ui.ToolsPanel;
import com.opentouchgaming.androidcore.ui.tutorial.TutorialDialog;

import java.util.ArrayList;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;


public class MainFragment extends Fragment implements ToolsPanel.Listener, EnginesPanel.Listener
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "MainFragment");
    }

    public enum FocusMode
    {
        GAMES, LAUNCH, ENGINE, TOOLS
    }

    public final int TOOL_BUTTON_GAMEPAD  = 0;
    public final int TOOL_BUTTON_SETTINGS = 1;
    public final int TOOL_BUTTON_LOG      = 2;
    public final int TOOL_BUTTON_INFO     = 3;
    public final int TOOL_BUTTON_HELP     = 4;
    public final int TOOL_BUTTON_EMAIL    = 5;

    // Set by the entry activity
    public static GameEngine[] gameEngines;

    // App data to be saved/loaded
    public AppData    appData;
    // Data to be saved for the current engine, currently the argument history
    public EngineData engineData;

    // Handel dpad arrows
    public Dpad dpadControl = new Dpad();

    // Left/right panels
    public EnginesPanel enginesLeftPanel;
    public ToolsPanel   toolsPanel;

    public SubGameRecyclerViewAdapter subGameAdapter;

    // UI elements
    public RecyclerView recyclerView;
    public ImageView    backgroundImageView;
    public TextView     titleTextView;
    public TextView     argsTextView;
    public ImageButton  swapVerImageButton;
    public ImageButton  startButton;
    public ImageButton  showArgsButton;
    public Drawable     subgameSeparatorLine; // So we cna change the color of the line

    public int selectedVersion = 0;

    public ArrayList<SubGame> availableSubGames = new ArrayList<>();
    public  SubGame selectedSubGame;

    public String argsFinal = "";
    //CustomArgs customArgs = new CustomArgs();

    // Current gamepad focus mode
    public FocusMode focusMode;

    public GameLauncherInterface launcher;

    private boolean multiselectEnable = false;
    private ArrayList<SubGame> multiselectGames = new ArrayList<>();


    public final ToolsPanel.ToolsPanelButton[] toolsButtons = new ToolsPanel.ToolsPanelButton[]
            {
                    new ToolsPanel.ToolsPanelButton(TOOL_BUTTON_GAMEPAD, "Gamepad",R.drawable.ic_videogame_asset_black_24dp),
                    new ToolsPanel.ToolsPanelButton(TOOL_BUTTON_SETTINGS, "Settings",R.drawable.ic_settings_black_24dp),
                    new ToolsPanel.ToolsPanelButton(TOOL_BUTTON_LOG, "View last log",R.drawable.ic_computer_black_24dp),
                    new ToolsPanel.ToolsPanelButton(TOOL_BUTTON_INFO,"Changes", R.drawable.ic_info_outline_black_24dp),
                    new ToolsPanel.ToolsPanelButton(TOOL_BUTTON_EMAIL, "Email log",R.drawable.ic_email_black_24dp),
                    new ToolsPanel.ToolsPanelButton(TOOL_BUTTON_HELP, "Tutorials",R.drawable.ic_help_green),
            };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainFragment()
    {
        log.log(D, "New instant created!");
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        for (GameEngine engine : gameEngines)
        {
            engine.init(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        log.log(D, "onCreateView");

        ScopedStorage.checkStorageOK(getActivity());

        View view = inflater.inflate(R.layout.fragment_alpha, container, false);

        boolean uiGroup = AppSettings.getBoolOption(getContext(),"group_similar_engines", false);
        enginesLeftPanel = new EnginesPanel(getContext(), view, gameEngines, uiGroup, this);

        toolsPanel = new ToolsPanel(getContext(), view, toolsButtons, this);

        recyclerView = view.findViewById(R.id.list);
        backgroundImageView = view.findViewById(R.id.imageview_doom_background);
        argsTextView = view.findViewById(R.id.textview_doom_args);

        swapVerImageButton = view.findViewById(R.id.imagebutton_change_version);
        swapVerImageButton.setBackgroundResource(R.drawable.focusable);
        swapVerImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                cycleVersion();
            }
        });

        // Title text and set font
        titleTextView = view.findViewById(R.id.textview_doom_title);
        Typeface face = Typeface.createFromAsset(getContext().getAssets(), "recharge_font.ttf");
        titleTextView.setTypeface(face);

        Context context = view.getContext();

        recyclerView.setFocusable(false); // We always intercept gamepad to control the list

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        subGameAdapter = new SubGameRecyclerViewAdapter(availableSubGames, input -> {

            if(multiselectEnable) {
                if (multiselectGames.contains(input))
                    multiselectGames.remove(input);
                else
                    multiselectGames.add(input);

                updateArgs();
            }
            return null;
        });

        recyclerView.setAdapter(subGameAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        subgameSeparatorLine = ResourcesCompat.getDrawable(getResources(), R.drawable.wad_separate_line, null);
        dividerItemDecoration.setDrawable(subgameSeparatorLine);
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v)
            {
                enginesLeftPanel.closeIfOpen();
                toolsPanel.closeIfOpen();
                selectSubGame(position);
            }
        });

        ItemClickSupport.addTo(recyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v)
            {
                selectSubGame(position);
                SubGame.DialogCallback callback = new SubGame.DialogCallback()
                {
                    @Override
                    public void dismiss()
                    {
                        refreshSubGames();
                        selectSubGame(engineData.selectedSubGamePos);
                    }
                };

                if (selectedSubGame != null)
                    selectedSubGame.edit(getActivity(), callback);
                return true;
            }
        });

        startButton = view.findViewById(R.id.imageview_game_start);

        showArgsButton = view.findViewById(R.id.imageview_doom_show_args);

        showArgsButton.setBackgroundResource(R.drawable.focusable);
        showArgsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new CustomArgsDialog(getActivity(), launcher.getRunDirectory(), engineData).dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        updateArgs();
                    }
                });
            }
        });
        // START game
        startButton.setBackgroundResource(R.drawable.focusable);
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                boolean ok = LicenseCheck.checkLicenseFile(getContext(), AppInfo.key);

                if (!ok)
                {
                    LicenseCheck.fetchLicense(getActivity(), true, AppInfo.key);
                } else
                {
                    launchGame(AppInfo.currentEngine, true, "");
                }
            }
        });

        setFocusMode(FocusMode.GAMES);

        recyclerView.setFocusable(false); // We always intercept gamepad to control the list
        recyclerView.setFocusableInTouchMode(false);

/*
        String path = getActivity().getExternalFilesDir(null).toString();
        String myfile = path  + "/myfile.txt";
        log.log(D," path = " + path + " ExTERN = " + Environment.getExternalStorageDirectory());

        Utils.mkdirs(getContext(),path);


        PrintWriter writer = null;
        try {
            writer = new PrintWriter( myfile, "UTF-8");
            writer.println("The first line");
            writer.println("The second line");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + myfile)));
*/
        return view;
    }

    public void launchGame(final GameEngine engine, boolean download, final String multiplayerArgs)
    {
        log.log(D, "ERROR, launchGame must be overridden");
    }

    public void setLauncher()
    {
        log.log(D, "ERROR, setLauncher must be overridden");
    }

    public View getExtraOptions()
    {
        // Override if we have extra options
        return null;
    }

    public  void setFocusMode(FocusMode mode)
    {
        log.log(D, "setFocusMode: mode = " + mode.toString());

        focusMode = mode;

        switch (focusMode)
        {
            case GAMES:
            {
                setLaunchButtonsFocus(false);
                break;
            }
            case LAUNCH:
            {
                setLaunchButtonsFocus(true);
                break;
            }
            case ENGINE:
            {
                setLaunchButtonsFocus(false);
                break;
            }
            case TOOLS:
            {
                setLaunchButtonsFocus(false);
                break;
            }
        }
    }

    public  void setLaunchButtonsFocus(boolean enabled)
    {
        startButton.setFocusable(enabled);
        showArgsButton.setFocusable(enabled);
        swapVerImageButton.setFocusable(enabled);
        //multiplayerButton.setFocusable(enabled);
        if (enabled)
            startButton.requestFocus();
    }

    public  void cycleVersion()
    {
        selectedVersion++;
        if (selectedVersion > AppInfo.currentEngine.versions.length - 1)
            selectedVersion = 0;

        // Update display
        selectEngine(AppInfo.currentEngine);
    }

    public  boolean onBackPressed()
    {
        boolean ret = false;
        if (enginesLeftPanel.isOpen())
        {
            enginesLeftPanel.close();
            ret = true;
        }

        if (toolsPanel.isOpen())
        {
            toolsPanel.close();
            ret = true;
        }

        return ret;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        AppData.saveToFile(AppInfo.internalFiles + "/AppData.dat", appData);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        // Show about dialog in 1 second
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (getActivity() != null)
                {
                    if (AboutDialog.showAbout(getActivity()))
                    {
                        AboutDialog.show(getActivity());
                        // Also open the panel so people know about it
                        enginesLeftPanel.open();
                    }
                }
            }
        }, 1000);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ScopedStorage.checkStorageOK(getActivity());
        updateAll();
    }

    public  void updateAll()
    {
        appData = AppData.loadFromFile(AppInfo.internalFiles + "/AppData.dat");

        enginesLeftPanel.closeIfOpen();

        String lastEngine = AppSettings.getStringOption(getContext(), "last_engine", null);
        if (lastEngine != null)
        {
            for (GameEngine engine : gameEngines)
            {
                if (lastEngine.equals(engine.engine.toString()))
                {
                    int lastVersion = AppSettings.getIntOption(getContext(), "last_version", 0);
                    if (lastVersion < engine.versions.length)
                    {
                        selectedVersion = lastVersion;
                    }

                    enginesLeftPanel.selectEngine(engine);
                }
            }
        }

        if (AppInfo.currentEngine == null)
        {
            enginesLeftPanel.selectEngine(gameEngines[0]);
        }

        refreshSubGames();
        selectSubGame(engineData.selectedSubGamePos);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        log.log(D, "onKeyDown: event = " + event.toString() + " keyCode = " + keyCode);

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD)
        {
            if (event.getRepeatCount() == 0)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_BUTTON_A:
                    {
                        if (focusMode == FocusMode.GAMES)
                        {
                            setFocusMode(FocusMode.LAUNCH);
                            return true;
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_BUTTON_B:
                    {
                        if (focusMode == FocusMode.LAUNCH)
                        {
                            setFocusMode(FocusMode.GAMES);
                            return true;
                        }
                    }
                    default:

                        break;
                }
            }
        }
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event)
    {
        //log.log(D, "onGenericMotionEvent: event = " + event.toString());

        if (Dpad.isDpadDevice(event))
        {
            int press = dpadControl.getDirectionPressed(event);
            switch (press)
            {
                case Dpad.LEFT: //Open left panel
                    // This is a mess. If launch buttons then we want left/right working
                    if (focusMode != FocusMode.GAMES)
                        return false;

                    // Close right if open
                    if (toolsPanel.isOpen())
                    {
                        toolsPanel.close();
                        return true;
                    }
                    // Open left
                    if (!enginesLeftPanel.isOpen())
                    {
                        enginesLeftPanel.open();
                        return true;
                    }

                    break;
                case Dpad.RIGHT:

                    if (focusMode != FocusMode.GAMES)
                        return false;

                    // Close left if open
                    if (enginesLeftPanel.isOpen())
                    {
                        enginesLeftPanel.close();
                        return true;
                    }

                    //Open right
                    if (!toolsPanel.isOpen())
                    {
                        toolsPanel.open();
                        return true;
                    }
                    break;
                case Dpad.UP:
                    if (focusMode == FocusMode.GAMES)
                    {
                        selectSubGame(engineData.selectedSubGamePos - 1);
                        return true;
                    }
                    break;
                case Dpad.DOWN:
                    if (focusMode == FocusMode.GAMES)
                    {
                        selectSubGame(engineData.selectedSubGamePos + 1);
                        return true;
                    }
                    break;
            }
        }
        return false;
    }


    public void selectEngine(GameEngine engine)
    {
        backgroundImageView.setImageResource(engine.iconRes);

        if (selectedVersion > engine.versions.length - 1)
            selectedVersion = 0;

        // Only DP can multiselect
        if( engine.engine == GameEngine.Engine.QUAKEDP)
            multiselectEnable = true;
        else
            multiselectEnable = false;

        subGameAdapter.setMultiSelect(multiselectEnable);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(500);
        backgroundImageView.startAnimation(fadeIn);

        if (engine.versions.length > 1)
        {
            swapVerImageButton.setVisibility(View.VISIBLE);
            swapVerImageButton.startAnimation(fadeIn);
        } else
        {
            swapVerImageButton.setVisibility(View.GONE);
        }

        titleTextView.setText(AppInfo.title + " | " + engine.title + " " + engine.versions[selectedVersion]);
        titleTextView.setTextColor(0xFF000000 | engine.color);

        // Change the color of the separating lines
        GradientDrawable shapeDrawable = (GradientDrawable) subgameSeparatorLine;
        shapeDrawable.setColor(0xFF000000 | engine.color);

        AppInfo.currentEngine = engine;

        engineData = appData.getEngineData(AppInfo.currentEngine.engine);

        setLauncher();

        AppSettings.setStringOption(getContext(), "last_engine", AppInfo.currentEngine.engine.toString());
        AppSettings.setIntOption(getContext(), "last_version", selectedVersion);

        refreshSubGames();
        selectSubGame(engineData.selectedSubGamePos);
    }


    public  void refreshSubGames()
    {
        launcher.updateSubGames(AppInfo.currentEngine, availableSubGames);
        subGameAdapter.notifyDataSetChanged();
    }

    public  void updateArgs()
    {
        if (selectedSubGame != null)
        {
            argsFinal = launcher.getArgs(AppInfo.currentEngine,selectedSubGame);

            for(SubGame sg : multiselectGames)
            {
                argsFinal +=  launcher.getArgs(AppInfo.currentEngine,sg);
            }

            argsFinal += " " + selectedSubGame.getExtraArgs();

            argsFinal += " " + engineData.getCurrentCustomArgs().getFinalArgs();
            argsTextView.setText( AppInfo.replaceRootPaths(argsFinal));
        } else
        {
            argsTextView.setText("Select game");
        }
    }

    public  void selectSubGame(int newPos)
    {
        multiselectGames.clear();

        if (availableSubGames.size() == 0)
            return;

        if (newPos >= availableSubGames.size() || newPos < 0)
            newPos = 0;

        engineData.selectedSubGamePos = newPos;

        // Make the iwad highlighted
        if (selectedSubGame != null)
        {
            selectedSubGame.selected = false;
        }

        selectedSubGame = availableSubGames.get(engineData.selectedSubGamePos);
        selectedSubGame.selected = true;
        subGameAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(engineData.selectedSubGamePos);
        updateArgs();
    }



    @Override
    public void engineSelected(GameEngine engine)
    {
        selectEngine(engine);
    }

    @Override
    public void engineConfig(GameEngine engine)
    {
        if (engine.engineOptions != null)
        {
            engine.engineOptions.showDialog(getActivity(), engine, selectedVersion, version ->
            {
                // For redraw of items incase changed layouts
                recyclerView.setAdapter(null);
                recyclerView.setLayoutManager(null);
                recyclerView.setAdapter(subGameAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                selectedVersion = version;
                selectEngine(AppInfo.currentEngine);
                return null;
            });
        }
    }


    @Override
    public void toolsOnClick(int code)
    {
        if (code == TOOL_BUTTON_GAMEPAD)
        {
            Intent intent = new Intent(getContext(), GamepadActivity.class);
            startActivity(intent);
        } else if (code == TOOL_BUTTON_SETTINGS)
        {

            new OptionsDialog(getActivity(), getExtraOptions())
            {
                public void dismiss()
                {
                    updateAll();
                }
            };

        } else if (code == TOOL_BUTTON_LOG)
        {
            new LogViewDialog(getActivity(), AppInfo.internalFiles + "/" + AppInfo.currentEngine.name, AppInfo.currentEngine.name);

        } else if (code == TOOL_BUTTON_INFO) {
            AboutDialog.show(getActivity());
        }
            else if (code == TOOL_BUTTON_EMAIL)
        {
            Utils.SendDebugEmail(getActivity(), AppInfo.emailAddress, AppInfo.packageId, AppInfo.internalFiles + "/" + AppInfo.currentEngine.name);
        } else if (code == TOOL_BUTTON_HELP)
        {
            new TutorialDialog(getActivity(), AppInfo.tutorials);
        }
    }

    /* This is pretty shit and confusing, opening the panel will change its own focus mode */
    @Override
    public void toolsPanelStateChange(boolean open)
    {
        if (open)
            setFocusMode(FocusMode.TOOLS);
        else
            setFocusMode(FocusMode.GAMES);
    }

    @Override
    public void enginePanelStateChange(boolean open)
    {
        if (open)
            setFocusMode(FocusMode.ENGINE);
        else
            setFocusMode(FocusMode.GAMES);
    }
}
