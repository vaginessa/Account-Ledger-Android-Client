package ndk.personal.account_ledger.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ndk.personal.account_ledger.R;
import ndk.personal.account_ledger.constants.API;
import ndk.personal.account_ledger.constants.API_Wrapper;
import ndk.personal.account_ledger.constants.Application_Specification;
import ndk.personal.account_ledger.models.Account;
import ndk.utils.Activity_Utils;
import ndk.utils.Date_Utils;
import ndk.utils.Toast_Utils;
import ndk.utils.Validation_Utils;
import ndk.utils.network_task.REST_GET_Task;
import ndk.utils.network_task.REST_Insert_Task_Wrapper;
import ndk.utils.network_task.REST_Select_Task;
import ndk.utils.network_task.REST_Select_Task_Wrapper;

public class Insert_Transaction_v2 extends AppCompatActivity {

    Context application_context;
    SharedPreferences settings;
    private ProgressBar login_progress;
    private Button button_date, button_to;
    private EditText edit_purpose;
    private EditText edit_amount;
    private Calendar calendar = Calendar.getInstance();
    private ScrollView login_form;
    AutoCompleteTextView autoCompleteTextView_to;
    String current_parent_account_id = "0";
    private ArrayList<Account> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction_v2);

        application_context = getApplicationContext();

        settings = getApplicationContext().getSharedPreferences(Application_Specification.APPLICATION_NAME, Context.MODE_PRIVATE);

        login_form = findViewById(R.id.login_form);
        Button button_submit = findViewById(R.id.button_submit);
        Button button_from = findViewById(R.id.button_from);
        button_to = findViewById(R.id.button_to);
        edit_amount = findViewById(R.id.edit_amount);
        edit_purpose = findViewById(R.id.edit_purpose);
        button_date = findViewById(R.id.button_date);
        login_progress = findViewById(R.id.login_progress);
        autoCompleteTextView_to = findViewById(R.id.autoCompleteTextView_to);

        associate_button_with_time_stamp();

        button_from.setText("From : " + getIntent().getStringExtra("CURRENT_ACCOUNT_FULL_NAME"));

        // Initialize
        final SwitchDateTimeDialogFragment dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                "Pick Time",
                "OK",
                "Cancel"
        );

        // Assign values
        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.set24HoursMode(true);
//        dateTimeFragment.setMaximumDateTime(calendar.getTime());

//        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime());
//        dateTimeFragment.setDefaultDateTime(new GregorianCalendar(2017, Calendar.MARCH, 4, 15, 20).getTime());

        // Or assign each element, default element is the current moment
//        dateTimeFragment.setDefaultHourOfDay(15);
//        dateTimeFragment.setDefaultMinute(20);
//        dateTimeFragment.setDefaultDay(4);
//        dateTimeFragment.setDefaultMonth(Calendar.MARCH);
//        dateTimeFragment.setDefaultYear(2017);

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(Date_Utils.normal_stripped_date_format);
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(Application_Specification.APPLICATION_NAME, e.getMessage());
        }

        // Set listener
        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {

            @Override
            public void onPositiveButtonClick(Date date) {
                // Date is get on positive button click
                calendar.set(Calendar.YEAR, dateTimeFragment.getYear());
                calendar.set(Calendar.MONTH, dateTimeFragment.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, dateTimeFragment.getDay());
                calendar.set(Calendar.HOUR_OF_DAY, dateTimeFragment.getHourOfDay());
                calendar.set(Calendar.MINUTE, dateTimeFragment.getMinute());

                associate_button_with_time_stamp();

                Log.d(Application_Specification.APPLICATION_NAME, "Selected : " + Date_Utils.date_to_mysql_date_time_string((calendar.getTime())));
                // dateTimeFragment.setDefaultDateTime(calendar.getTime());
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Date is get on negative button click
            }
        });

        button_date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Show
                dateTimeFragment.show(getSupportFragmentManager(), "dialog_time");
            }
        });

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempt_insert_Transaction();
            }
        });

        button_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_to_account();
            }
        });

        bind_auto_text_view();
    }

    private void bind_auto_text_view() {

        REST_Select_Task.Async_Response_JSON_array async_response_json_array = new REST_Select_Task.Async_Response_JSON_array() {

            @Override
            public void processFinish(JSONArray json_array) {

                accounts = new ArrayList<>();
                ArrayList<String> account_full_names = new ArrayList<>();

                for (int i = 1; i < json_array.length(); i++) {

                    try {
                        accounts.add(new Account(json_array.getJSONObject(i).getString("account_type"), json_array.getJSONObject(i).getString("account_id"), json_array.getJSONObject(i).getString("notes"), json_array.getJSONObject(i).getString("parent_account_id"), json_array.getJSONObject(i).getString("owner_id"), json_array.getJSONObject(i).getString("name"), json_array.getJSONObject(i).getString("commodity_type"), json_array.getJSONObject(i).getString("commodity_value")));
                        account_full_names.add(json_array.getJSONObject(i).getString("name"));

                    } catch (JSONException e) {

                        Toast.makeText(getApplicationContext(), "Error : " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        Log.d(Application_Specification.APPLICATION_NAME, "Error : " + e.getLocalizedMessage());
                    }
                }

                //Creating the instance of ArrayAdapter containing list of fruit names
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Insert_Transaction_v2.this, android.R.layout.select_dialog_item, account_full_names);

                autoCompleteTextView_to.setThreshold(1);//will start working from first character
                autoCompleteTextView_to.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                autoCompleteTextView_to.setTextColor(Color.RED);
                autoCompleteTextView_to.showDropDown();

            }
        };

        REST_Select_Task_Wrapper.execute(REST_GET_Task.get_Get_URL(API_Wrapper.get_http_API(API.select_User_Accounts), new Pair[]{new Pair<>("user_id", settings.getString("user_id", "0")), new Pair<>("parent_account_id", current_parent_account_id)}), this, Application_Specification.APPLICATION_NAME, new Pair[]{}, async_response_json_array, true, true);
    }

    private void select_to_account() {

        Activity_Utils.start_activity_with_string_extras(this, List_Accounts.class, new Pair[]{new Pair<>("HEADER_TITLE", "NA"), new Pair<>("PARENT_ACCOUNT_ID", "0"), new Pair<>("ACTIVITY_FOR_RESULT_FLAG", String.valueOf(true)), new Pair<>("CURRENT_ACCOUNT_COMMODITY_TYPE", "CURRENCY"), new Pair<>("CURRENT_ACCOUNT_TYPE", "Assets"), new Pair<>("CURRENT_ACCOUNT_COMMODITY_VALUE", "INR"), new Pair<>("CURRENT_ACCOUNT_TAXABLE", String.valueOf(false)), new Pair<>("CURRENT_ACCOUNT_PLACE_HOLDER", String.valueOf(false))}, true, 0);
    }

    String to_selected_account_id = "0";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            button_to.setText("To : " + data.getStringExtra("SELECTED_ACCOUNT_FULL_NAME"));
            to_selected_account_id = data.getStringExtra("SELECTED_ACCOUNT_ID");

        }
    }

    private void associate_button_with_time_stamp() {
        button_date.setText(Date_Utils.normal_date_time_format_words.format(calendar.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.insert_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_item_view_pass_book) {

            Activity_Utils.start_activity_with_string_extras(this, Clickable_Pass_Book_Bundle.class, new Pair[]{new Pair<>("URL", REST_GET_Task.get_Get_URL(API_Wrapper.get_http_API(API.select_User_Transactions_v2), new Pair[]{new Pair<>("user_id", settings.getString("user_id", "0")), new Pair<>("account_id", getIntent().getStringExtra("CURRENT_ACCOUNT_ID"))})), new Pair<>("application_name", Application_Specification.APPLICATION_NAME), new Pair<>("V2_FLAG", getIntent().getStringExtra("CURRENT_ACCOUNT_ID"))
            }, false, 0);
        }

        return super.onOptionsItemSelected(item);
    }

    private void attempt_insert_Transaction() {

        if (to_selected_account_id.equals("0")) {
            Toast_Utils.longToast(this, "Please select To A/C...");
        } else {

            Validation_Utils.reset_errors(new EditText[]{edit_purpose, edit_amount});
            Pair<Boolean, EditText> empty_check_result = Validation_Utils.empty_check(new Pair[]{new Pair<>(edit_amount, "Please Enter Valid Amount..."), new Pair<>(edit_purpose, "Please Enter Purpose...")});

            if (empty_check_result.first) {
                // There was an error; don't attempt login and focus the first form field with an error.
                if (empty_check_result.second != null) {
                    empty_check_result.second.requestFocus();
                }
            } else {

                Pair<Boolean, EditText> zero_check_result = Validation_Utils.zero_check(new Pair[]{new Pair<>(edit_amount, "Please Enter Valid Amount...")});
                if (zero_check_result.first) {
                    if (zero_check_result.second != null) {
                        zero_check_result.second.requestFocus();
                    }
                } else {
                    execute_insert_Transaction_Task();
                }
            }
        }
    }

    private void execute_insert_Transaction_Task() {

        REST_Insert_Task_Wrapper.execute(this, API_Wrapper.get_http_API(API.insert_Transaction_v2), this, login_progress, login_form, Application_Specification.APPLICATION_NAME, new Pair[]{new Pair<>("event_date_time", Date_Utils.date_to_mysql_date_time_string(calendar.getTime())), new Pair<>("user_id", settings.getString("user_id", "0")), new Pair<>("particulars", edit_purpose.getText().toString()), new Pair<>("amount", edit_amount.getText().toString()), new Pair<>("from_account_id", getIntent().getStringExtra("CURRENT_ACCOUNT_ID")), new Pair<>("to_account_id", to_selected_account_id)}, edit_purpose, new EditText[]{edit_purpose, edit_amount});
    }
}
