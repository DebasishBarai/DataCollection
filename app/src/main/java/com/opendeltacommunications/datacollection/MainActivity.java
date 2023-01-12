package com.opendeltacommunications.datacollection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String AUTHORITY=
            BuildConfig.APPLICATION_ID+".provider";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    String[] tabArray;
    EditText editText;
    String fragmentTag;
    Fragment currentFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the app bar menu
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure:
                // Execute code for menu item configure
                return true;
            case R.id.reset:
                // Execute code for menu item reset
                // Get the current tab index from the view pager
                int currentTabIndex = viewPager.getCurrentItem();

                // Get the tag of the fragment associated with the current tab
                fragmentTag = "android:switcher:" + viewPager.getId() + ":" + currentTabIndex;

                // Get the fragment associated with the current tab
                currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

                reset_fragment(currentFragment);

                // Show a toast message
                Toast.makeText(this, "Reset done successfully for the current fragment", Toast.LENGTH_LONG).show();

                return true;
            case R.id.reset_all:
                // Execute code for menu item reset all
                /*int tabCount= viewPager.getAdapter().getCount();
                for (int i=0; i<tabCount; i++){
                    // Get the tag of the fragment associated with the current tab
                    fragmentTag = "android:switcher:" + viewPager.getId() + ":" + i;

                    // Get the fragment associated with the current tab
                    currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

                    reset_fragment(currentFragment);
                }

                // Show a toast message
                Toast.makeText(this, "Reset done successfully for all fragments", Toast.LENGTH_LONG).show();
*/                return true;
            case R.id.submit:
                try {
                    //get the preferences object
                    SharedPreferences preferences = getSharedPreferences("tabs", MODE_PRIVATE);

                    //get the UPS names
                    String getStringSet = preferences.getString("UPS", null);

                    tabArray = getStringSet.substring(1, getStringSet.length() - 1).split(", ");

                    //Build the table for the data
                    // Create a new workbook
                    Workbook workbook = new HSSFWorkbook();

                    // Create a new sheet
                    Sheet sheet = workbook.createSheet("UPS Data");

                    // Create the header row and add data to it
                    Row row = sheet.createRow(0);
                    row.createCell(0).setCellValue("UPS");
                    row.createCell(1).setCellValue("Ch-I O/P Voltage (V)");
                    row.createCell(2).setCellValue("Ch-I O/P Current (A)");
                    row.createCell(3).setCellValue("Ch-I Battery Voltage (V)");
                    row.createCell(4).setCellValue("Ch-I Battery Current (A)");
                    row.createCell(5).setCellValue("Ch-II O/P Voltage (V)");
                    row.createCell(6).setCellValue("Ch-II O/P Current (A)");
                    row.createCell(7).setCellValue("Ch-II Battery Voltage (V)");
                    row.createCell(8).setCellValue("Ch-II Battery Current (A)");
                    row.createCell(9).setCellValue("UPS Bypass Voltage (V)");
                    row.createCell(10).setCellValue("UPS Room temp (degC)");
                    row.createCell(11).setCellValue("Remarks");

                    for (int i = 0; i < tabArray.length; i++) {
                        String[] tabData;
                        String getString = preferences.getString(tabArray[i], "null");
                        if (!(getString == "null") && !(getString.equalsIgnoreCase("[,,,,,,,,,,]"))) {
                            tabData = getString.substring(1, getString.length() - 1).split(", ");
                            // Create a new row for each UPS and add data to it
                            row = sheet.createRow(i + 1);
                            row.createCell(0).setCellValue(tabArray[i]);

                            for (int j = 0; j < tabData.length; j++) {
                                row.createCell(j + 1).setCellValue(tabData[j]);
                            }
                        }

                    }

                    // Save the workbook to a temporary file
                    File file = new File(getExternalFilesDir(null), "ups.xls");

                    try (FileOutputStream out = new FileOutputStream(file)) {
                        workbook.write(out);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Create an Intent to send the file as an attachment
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/vnd.ms-excel");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"debasishbaraiju@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Workbook");
                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, AUTHORITY, file));

                    // Start the email app
                    startActivity(Intent.createChooser(intent, "Send email"));

                    // Schedule the file for deletion when the app exits
                    file.deleteOnExit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the tab layout and view pager
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);


        SharedPreferences preferences = getSharedPreferences("tabs", MODE_PRIVATE);

        //Hardcode the sharedPreferences with UPS names
        SharedPreferences.Editor editor = preferences.edit();

        String[] tabArraySet = {"TPS Vertiv", "TPS Gutor", "SS-2B", "SS-4", "LOB", "DHDS", "MSQ", "OHCU", "HGU", "SS-19", "SS-20", "FOB C/R", "O/S R/R", "Flare R/R"};

        String setStringSet = Arrays.toString(tabArraySet);

        editor.putString("UPS", setStringSet);

        editor.apply();

        // Load the tab names from the SharedPreferences

        String getStringSet = preferences.getString("UPS", null);

        tabArray = getStringSet.substring(1, getStringSet.length() - 1).split(", ");

        // Create the pager adapter and set it to the view pager
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Set the tab layout and view pager to use the same adapter
        tabLayout.setupWithViewPager(viewPager);

    }

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CommonFragment getItem(int position) {
            return new CommonFragment();
        }


        @Override
        public int getCount() {
            // Return the number of fragments
            return tabArray.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Return the tab title for the given position
            return tabArray[position];
        }
    }

    public void saveData(View view) {

        // Get the current tab index from the view pager
        int currentTabIndex = viewPager.getCurrentItem();

        // Get the tab object for the current tab
        TabLayout.Tab currentTab = tabLayout.getTabAt(currentTabIndex);

        // Get the tag of the fragment associated with the current tab
        String fragmentTag = "android:switcher:" + viewPager.getId() + ":" + currentTabIndex;

        // Get the fragment associated with the current tab
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

        // Get the tab title
        String tabTitle = currentTab.getText().toString();

        String[] tabData = new String[11];

        for (int i = 0; i < 11; i++) {

            switch (i) {
                case 0:
                    editText = currentFragment.getView().findViewById(R.id.ups1_op_v);
                    break;
                case 1:
                    editText = currentFragment.getView().findViewById(R.id.ups1_op_c);
                    break;
                case 2:
                    editText = currentFragment.getView().findViewById(R.id.ups1_batt_v);
                    break;
                case 3:
                    editText = currentFragment.getView().findViewById(R.id.ups1_batt_c);
                    break;
                case 4:
                    editText = currentFragment.getView().findViewById(R.id.ups2_op_v);
                    break;
                case 5:
                    editText = currentFragment.getView().findViewById(R.id.ups2_op_c);
                    break;
                case 6:
                    editText = currentFragment.getView().findViewById(R.id.ups2_batt_v);
                    break;
                case 7:
                    editText = currentFragment.getView().findViewById(R.id.ups2_batt_c);
                    break;
                case 8:
                    editText = currentFragment.getView().findViewById(R.id.ups_byp_v);
                    break;
                case 9:
                    editText = currentFragment.getView().findViewById(R.id.ups_room_temp);
                    break;
                case 10:
                    editText = currentFragment.getView().findViewById(R.id.ups_remarks);
                    break;
                default:
                    break;
            }
            if (editText.getText().toString() == "") {
                tabData[i] = " ";
            } else {
                tabData[i] = editText.getText().toString();
            }
        }

        //save the array tabData in shared preferences
        String set = Arrays.toString(tabData);

        SharedPreferences preferences = getSharedPreferences("tabs", MODE_PRIVATE);
        preferences.edit().putString(tabTitle, set).apply();

        // Show a toast message
        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_LONG).show();
    }

    public void reset_fragment(Fragment currentFragment){

        if(currentFragment==null) {
            return;
        }

        //Get all the edittext fields and set the string value to ""
        editText = currentFragment.getView().findViewById(R.id.ups1_op_v);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups1_op_c);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups1_batt_v);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups1_batt_c);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups2_op_v);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups2_op_c);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups2_batt_v);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups2_batt_c);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups_byp_v);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups_room_temp);
        editText.setText("");
        editText = currentFragment.getView().findViewById(R.id.ups_remarks);
        editText.setText("");
    }
}

