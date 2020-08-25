package com.jaekapps.expensetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

public class ExpenseIncomeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    int length;
    private BEIAmount beiAmount;
    private boolean itemFound;
    private CardView addCardView, cancelCardView, dotCardView, eightCardView, equalOrOkCardView, expenseCardView, fiveCardView,
            fourCardView, incomeCardView, nineCardView, oneCardView, sevenCardView, sixCardView, subcategoryCardView,
            subtractCardView, threeCardView, todayCardView, twoCardView, zeroCardView;
    private CategorySharedPreferences categorySharedPreferences;
    private char[] amount;
    private DatabaseReference userDBReference;
    private DatePickerDialog datePickerDialog;
    private int currentDay, currentMonth, currentYear, in, selectedDay, selectedMonth, selectedYear;
    private String balance, currentItemAmount, date, expense, income, month, previousItemAmount, text, totalAmount, userId;
    private TextView amountTextView, itemNameTextView, labelTextView, subcategoryTextView, todaysDateTextView;
    String category;

    private boolean checkCategoryAndItemName() {

        boolean status = false;

        if (categorySharedPreferences.readCategoryName().equals("")) {

            showToast("Please, select Expense or Income.");

        } else if (categorySharedPreferences.readCategoryName().equals("Expense") || categorySharedPreferences.readCategoryName().equals("Income")) {

            if (itemNameTextView.getText().equals("ADD ITEM")) {

                showToast("Please, select an item.");

            } else {

                status = true;

            }

        }

        return status;
    }

    private String addCurAmAndPreAm(float currentItemAmount, float previousItemAmount) {

        return  String.valueOf(currentItemAmount + previousItemAmount);
    }

    private String calculateTotalAmount(String currentItemAmount, String previousItemAmount) {

        String totalAmount;

        if (currentItemAmount.contains(".")) {

            totalAmount = addCurAmAndPreAm(Float.parseFloat(currentItemAmount), Float.parseFloat(previousItemAmount));

        } else {

            if (previousItemAmount.contains(".")) {

                totalAmount = addCurAmAndPreAm(Float.parseFloat(currentItemAmount), Float.parseFloat(previousItemAmount));

            } else {

                totalAmount = String.valueOf(Long.parseLong(currentItemAmount) + Long.parseLong(previousItemAmount));

            }

        }

        return totalAmount;
    }

    private String findMonth(int currentMonth) {

        String month = "";

        switch (currentMonth) {

            case 1:
                month = "Jan";
                break;

            case 2:
                month = "Feb";
                break;

            case 3:
                month = "Mar";
                break;

            case 4:
                month = "Apr";
                break;

            case 5:
                month = "May";
                break;

            case 6:
                month = "June";
                break;

            case 7:
                month = "July";
                break;

            case 8:
                month = "Aug";
                break;

            case 9:
                month = "Sep";
                break;

            case 10:
                month = "Oct";
                break;

            case 11:
                month = "Nov";
                break;

            case 12:
                month = "Dec";
                break;

        }

        return month;
    }

    private void addAmountToTextView(String number) {

        if (!amountTextView.getText().equals("0")) {

            text = "";
            amount = amountTextView.getText().toString().toCharArray();
            int operatorPos = 0;

            for (in = 0; in < amount.length; in++) {

                if (amount[in] == '+' || amount[in] == '-') {

                    operatorPos = in;
                    break;

                }

            }

            if (amount[operatorPos] == '+' || amount[operatorPos] == '-') {

                checkIfDotIsPresentOrNot(amount, operatorPos + 1, number);

            } else {

                checkIfDotIsPresentOrNot(amount, 0, number);

            }

        } else if (amountTextView.getText().toString().equals("0")) {

            text = number;
            amountTextView.setText(text);

        }

    }

    private void addOperatorToTextView(String operatorSign) {

        if (!amountTextView.getText().equals("0")) {

            text = "";
            text = amountTextView.getText().toString();
            amount = text.toCharArray();
            int pos = amount.length;
            pos = pos - 1;
            StringBuilder stringBuilder = new StringBuilder();

            if (amount[pos] == '+' || amount[pos] == '-') {

                text = "";

                for (in = 0; in <= pos; in++) {

                    stringBuilder.append(amount[in]);

                }

                text = stringBuilder.toString();
                amountTextView.setText(text);

            } else {

                int operatorPos = 0;

                for (in = 0; in < amount.length; in++) {

                    if (amount[in] == '+' || amount[in] == '-') {

                        operatorPos = in;
                        break;

                    }

                }

                if (operatorPos != in) {

                    text = amountTextView.getText().toString() + operatorSign;
                    amountTextView.setText(text);

                } else {

                    performTheOperation(operatorPos, operatorSign);

                }

            }

        }

    }

    private void calculateBEIIfDataTypeIsSameForExpense() {

        if (amountTextView.getText().toString().contains(".")) {

            float totalExpense = Float.parseFloat(expense) + Float.parseFloat(amountTextView.getText().toString());
            balance = String.valueOf(Float.parseFloat(income) - totalExpense);
            expense = String.valueOf(totalExpense);

        } else {

            long totalExpense = Long.parseLong(expense) + Long.parseLong(amountTextView.getText().toString());
            balance = String.valueOf(Long.parseLong(income) - totalExpense);
            expense = String.valueOf(totalExpense);

        }

    }

    private void calculateBEIIfDataTypeIsSameForIncome() {

        if (amountTextView.getText().toString().contains(".")) {

            float totalIncome = Float.parseFloat(income) + Float.parseFloat(amountTextView.getText().toString());
            balance = String.valueOf(totalIncome - Float.parseFloat(expense));
            income = String.valueOf(totalIncome);

        } else {

            long totalIncome = Long.parseLong(income) + Long.parseLong(amountTextView.getText().toString());
            balance = String.valueOf(totalIncome - Long.parseLong(expense));
            income = String.valueOf(totalIncome);

        }

    }

    private void checkIfDotIsPresentOrNot(char[] amount, int index, String number) {

        int flag = 0, pos = 0;

        for (in = index; in < amount.length; in++) {

            if (amount[in] == '.') {

                pos = in;
                break;

            }

        }

        if (amount[pos] == '.') {

            for (in = pos + 1; in < amount.length; in++) {

                flag++;

            }

            if (flag != 3) {

                text = amountTextView.getText().toString() + number;
                amountTextView.setText(text);

            }

        } else {

            text = amountTextView.getText().toString() + number;
            amountTextView.setText(text);

        }

    }

    private void checkIfDotIsPresentBeforeOrAfterOfOperator(char[] amount, int operatorPos) {

        if (amount[operatorPos - 1] == '.' || operatorPos == amount.length - 1 ||amount[amount.length - 1] == '.') {

            amountTextView.setText("0");
            showToast("Please, enter a valid amount.");

        } else {

            performTheOperation(operatorPos, "");

        }

    }

    private void performTheOperation(int operatorPos, String operatorSign) {

        StringBuilder firstNoStringBuilder = new StringBuilder();
        StringBuilder secondNoStringBuilder = new StringBuilder();

        for (in = 0; in < operatorPos; in++) {

            firstNoStringBuilder = firstNoStringBuilder.append(amount[in]);

        }

        for (in = operatorPos + 1; in < amount.length; in++) {

            secondNoStringBuilder = secondNoStringBuilder.append(amount[in]);

        }

        String firstNoString = firstNoStringBuilder.toString();
        String secondNoString = secondNoStringBuilder.toString();
        String operator = String.valueOf(amount[operatorPos]);

        if (operator.equals("+")) {

            if (firstNoString.contains(".") || secondNoString.contains(".")) {

                float firstNo = Float.parseFloat(firstNoString);
                float secondNo = Float.parseFloat(secondNoString);
                float addition = firstNo + secondNo;
                text = addition + operatorSign;

            } else {

                long firstNo = Long.parseLong(firstNoString);
                long secondNo = Long.parseLong(secondNoString);
                long addition = firstNo + secondNo;
                text = addition + operatorSign;

            }

            amountTextView.setText(text);

        } else if (operator.equals("-")) {

            if (firstNoString.contains(".") || secondNoString.contains(".")) {

                float firstNo = Float.parseFloat(firstNoString);
                float secondNo = Float.parseFloat(secondNoString);

                if (firstNo < secondNo) {

                    text = "0";
                    showToast("Please, enter valid amount.");

                } else {

                    float subtraction = firstNo - secondNo;

                    if (subtraction == 0) {

                        text = "0";

                    } else {

                        text = subtraction + operatorSign;

                    }

                }

            } else {

                long firstNo = Long.parseLong(firstNoString);
                long secondNo = Long.parseLong(secondNoString);

                if (firstNo < secondNo) {

                    text = "0";
                    showToast("Please, enter valid amount.");

                } else {

                    long subtraction = firstNo - secondNo;

                    if (subtraction == 0) {

                        text = "0";

                    } else {

                        text = subtraction + operatorSign;

                    }

                }

            }

            amountTextView.setText(text);

        }

    }

    private void initialization() {

        addCardView = findViewById(R.id.addCardView);
        amountTextView = findViewById(R.id.amountTextView);
        beiAmount = new BEIAmount();
        Calendar calendar = Calendar.getInstance();
        cancelCardView = findViewById(R.id.cancelCardView);
        categorySharedPreferences = new CategorySharedPreferences(this);
        categorySharedPreferences.writeCategoryName("");
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);
        datePickerDialog = new android.app.DatePickerDialog(
                this,
                this,
                currentYear,
                currentMonth,
                currentDay
        );
        currentMonth = currentMonth + 1;
        date = currentMonth + "/" + currentDay;
        dotCardView = findViewById(R.id.dotCardView);
        eightCardView = findViewById(R.id.eightCardView);
        equalOrOkCardView = findViewById(R.id.equalOrOkCardView);
        expenseCardView = findViewById(R.id.expenseCardView);
        fiveCardView = findViewById(R.id.fiveCardView);
        fourCardView = findViewById(R.id.fourCardView);
        incomeCardView = findViewById(R.id.incomeCardView);
        itemNameTextView = findViewById(R.id.itemNameTextView);
        labelTextView = findViewById(R.id.labelTextView);
        month = findMonth(currentMonth);
        nineCardView = findViewById(R.id.nineCardView);
        oneCardView = findViewById(R.id.oneCardView);
        selectedDay = currentDay;
        selectedMonth = currentMonth;
        selectedYear = currentYear;
        sevenCardView = findViewById(R.id.sevenCardView);
        sixCardView = findViewById(R.id.sixCardView);
        subcategoryCardView = findViewById(R.id.subcategoryCardView);
        subcategoryTextView = findViewById(R.id.subcategoryTextView);
        subtractCardView = findViewById(R.id.subtractCardView);
        threeCardView = findViewById(R.id.threeCardView);
        todayCardView = findViewById(R.id.todayCardView);
        todaysDateTextView = findViewById(R.id.todaysDateTextView);
        todaysDateTextView.setText(date);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Expense-Income");

        }

        twoCardView = findViewById(R.id.twoCardView);
        userDBReference = FirebaseDatabase.getInstance().getReference("User");
        UserIdConfigActivity userIdConfigActivity = new UserIdConfigActivity(this);
        userId = userIdConfigActivity.getUserID();
        zeroCardView = findViewById(R.id.zeroCardView);
    }

    private void initializeOnClickListener() {

        addCardView.setOnClickListener(this);
        cancelCardView.setOnClickListener(this);
        dotCardView.setOnClickListener(this);
        eightCardView.setOnClickListener(this);
        equalOrOkCardView.setOnClickListener(this);
        expenseCardView.setOnClickListener(this);
        fiveCardView.setOnClickListener(this);
        fourCardView.setOnClickListener(this);
        incomeCardView.setOnClickListener(this);
        nineCardView.setOnClickListener(this);
        oneCardView.setOnClickListener(this);
        sevenCardView.setOnClickListener(this);
        sixCardView.setOnClickListener(this);
        subcategoryCardView.setOnClickListener(this);
        subtractCardView.setOnClickListener(this);
        threeCardView.setOnClickListener(this);
        todayCardView.setOnClickListener(this);
        twoCardView.setOnClickListener(this);
        zeroCardView.setOnClickListener(this);
    }

    private void showToast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateTheBEIAmount(String balance, String expense, String income) {

        beiAmount.setBalance(balance);
        beiAmount.setExpense(expense);
        beiAmount.setIncome(income);
        month = findMonth(selectedMonth);
        final String category = categorySharedPreferences.readCategoryName() + "_Category";
        final String date;
        final String itemName = itemNameTextView.getText().toString();

        if (String.valueOf(selectedDay).length() == 1) {

            if (String.valueOf(selectedMonth).length() == 1) {

                date = "0" + selectedDay + "_" + "0" + selectedMonth + "_" + selectedYear;

            } else {

                date = "0" + selectedDay + "_" + selectedMonth + "_" + selectedYear;

            }

        } else {

            if (String.valueOf(selectedMonth).length() == 1) {

                date = selectedDay + "_" + "0" + selectedMonth + "_" + selectedYear;

            } else {

                date = selectedDay + "_" + selectedMonth + "_" + selectedYear;

            }

        }

        userDBReference.child(userId)
                .child("BEIAmount")
                .child(String.valueOf(selectedYear))
                .child(month)
                .setValue(beiAmount)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            updateTheItemCategoryAmount(category, date, itemName, month);

                        } else {

                            showToast(Objects.requireNonNull(task.getException()).getMessage());

                        }

                    }
                });

    }

    private void updateTheItemAmount(final String category, String date, String itemAmount, String itemName) {

        userDBReference.child(userId)
                .child(category)
                .child(String.valueOf(selectedYear))
                .child(month)
                .child(date)
                .child(itemName)
                .setValue(itemAmount)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Intent intent = new Intent();
                            intent.putExtra("category", category);
                            intent.putExtra("month", month);
                            intent.putExtra("year", String.valueOf(selectedYear));
                            setResult(RESULT_OK, intent);
                            finish();

                        } else {

                            showToast(Objects.requireNonNull(task.getException()).getMessage());

                        }

                    }
                });
    }

    private void updateTheItemCategoryAmount(final String category, final String date, final String itemName, String month) {

        currentItemAmount = amountTextView.getText().toString();
        userDBReference.child(userId)
                .child(category)
                .child(String.valueOf(selectedYear))
                .child(month)
                .child(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                                if (Objects.equals(itemSnapshot.getKey(), itemName)) {

                                    itemFound = true;
                                    previousItemAmount = itemSnapshot.getValue(String.class);
                                    break;

                                }

                            }

                            if (itemFound) {

                                totalAmount = calculateTotalAmount(currentItemAmount, previousItemAmount);
                                updateTheItemAmount(
                                        category,
                                        date,
                                        totalAmount,
                                        itemName
                                );

                            } else {

                                updateTheItemAmount(
                                        category,
                                        date,
                                        amountTextView.getText().toString(),
                                        itemName
                                );

                            }

                        } else {

                            updateTheItemAmount(
                                    category,
                                    date,
                                    amountTextView.getText().toString(),
                                    itemName
                            );

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Log.e("database_error", error.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {

                try {

                    if (data != null) {

                        itemNameTextView.setText(data.getStringExtra("item_name"));

                    }

                } catch (NullPointerException e) {

                    e.printStackTrace();

                }

            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_income);

        initialization();
        initializeOnClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.create_new_user_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            finish();

        } else if (item.getItemId() == R.id.saveInformation) {

            if (categorySharedPreferences.readCategoryName().equals("")) {

                showToast("Please, select Expense or Income.");

            } else if (categorySharedPreferences.readCategoryName().equals("Expense") || categorySharedPreferences.readCategoryName().equals("Income")){

                if (itemNameTextView.getText().equals("ADD ITEM")) {

                    showToast("Please, select an item.");

                } else {

                    if (amountTextView.getText().equals("0")) {

                        showToast("Please, enter amount.");

                    } else if (amountTextView.getText().equals("0.0") || amountTextView.getText().equals("0.00") || amountTextView.getText().equals("0.000")) {

                        amountTextView.setText("0");
                        showToast("Please, enter amount.");

                    } else {

                        int operatorPos = 0;
                        text = amountTextView.getText().toString();
                        amount = text.toCharArray();

                        for (in = 0; in < amount.length; in++) {

                            if (amount[in] == '+' || amount[in] == '-') {

                                operatorPos = in;
                                break;

                            }

                        }

                        if (amount[operatorPos] == '+' || amount[operatorPos] == '-') {

                            checkIfDotIsPresentBeforeOrAfterOfOperator(amount, operatorPos);

                        } else {

                            int pos = 0;
                            text = amountTextView.getText().toString();
                            amount = text.toCharArray();

                            for (in = 0; in < amount.length; in++) {

                                if (amount[in] == '.') {

                                    pos = in;
                                    break;

                                }

                            }

                            if (amount[pos] == '.' && pos == amount.length - 1) {

                                amountTextView.setText("0");
                                showToast("Please, enter a valid amount.");

                            } else {

                                month = findMonth(selectedMonth);
                                userDBReference.child(userId)
                                        .child("BEIAmount")
                                        .child(String.valueOf(selectedYear))
                                        .child(month)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {

                                                    beiAmount = snapshot.getValue(BEIAmount.class);

                                                    if (beiAmount != null) {

                                                        balance = beiAmount.getBalance();
                                                        expense = beiAmount.getExpense();
                                                        income = beiAmount.getIncome();

                                                        if (categorySharedPreferences.readCategoryName().equals("Expense")) {

                                                            if (expense.equals("0")) {

                                                                if (income.equals("0")) {

                                                                    calculateBEIIfDataTypeIsSameForExpense();

                                                                } else if (income.contains(".")) {

                                                                    if (amountTextView.getText().toString().contains(".")) {

                                                                        float totalExpense = Float.parseFloat(expense) + Float.parseFloat(amountTextView.getText().toString());
                                                                        balance = String.valueOf(Float.parseFloat(income) - totalExpense);
                                                                        expense = String.valueOf(totalExpense);

                                                                    } else {

                                                                        long totalExpense = Long.parseLong(expense) + Long.parseLong(amountTextView.getText().toString());
                                                                        expense = String.valueOf(totalExpense);
                                                                        balance = String.valueOf(Float.parseFloat(income) - Float.parseFloat(expense));

                                                                    }

                                                                } else {

                                                                    calculateBEIIfDataTypeIsSameForExpense();

                                                                }

                                                                updateTheBEIAmount(balance, expense, income);

                                                            } else if (expense.contains(".")) {

                                                                float totalExpense = Float.parseFloat(expense) + Float.parseFloat(amountTextView.getText().toString());
                                                                balance = String.valueOf(Float.parseFloat(income) - totalExpense);
                                                                expense = String.valueOf(totalExpense);
                                                                updateTheBEIAmount(balance, expense, income);

                                                            } else {

                                                                if (income.equals("0")) {

                                                                    calculateBEIIfDataTypeIsSameForExpense();

                                                                } else if (income.contains(".")) {

                                                                    float totalExpense = Float.parseFloat(expense) + Float.parseFloat(amountTextView.getText().toString());
                                                                    balance = String.valueOf(Float.parseFloat(income) - totalExpense);
                                                                    expense = String.valueOf(totalExpense);

                                                                } else {

                                                                    calculateBEIIfDataTypeIsSameForExpense();

                                                                }

                                                                updateTheBEIAmount(balance, expense, income);

                                                            }

                                                        } else if (categorySharedPreferences.readCategoryName().equals("Income")) {

                                                            if (expense.equals("0")) {

                                                                if (income.equals("0")) {

                                                                    calculateBEIIfDataTypeIsSameForIncome();

                                                                } else if (income.contains(".")) {

                                                                    float totalIncome = Float.parseFloat(income) + Float.parseFloat(amountTextView.getText().toString());
                                                                    balance = String.valueOf(totalIncome - Float.parseFloat(expense));
                                                                    income = String.valueOf(totalIncome);

                                                                } else {

                                                                    calculateBEIIfDataTypeIsSameForIncome();

                                                                }

                                                                updateTheBEIAmount(balance, expense, income);

                                                            } else if (expense.contains(".")) {

                                                                float totalIncome = Float.parseFloat(income) + Float.parseFloat(amountTextView.getText().toString());
                                                                balance = String.valueOf(totalIncome - Float.parseFloat(expense));
                                                                income = String.valueOf(totalIncome);
                                                                updateTheBEIAmount(balance, expense, income);

                                                            } else {

                                                                if (income.equals("0")) {

                                                                    calculateBEIIfDataTypeIsSameForIncome();

                                                                } else if (income.contains(".")) {

                                                                    float totalIncome = Float.parseFloat(income) + Float.parseFloat(amountTextView.getText().toString());
                                                                    balance = String.valueOf(totalIncome - Float.parseFloat(expense));
                                                                    income = String.valueOf(totalIncome);

                                                                } else {

                                                                    calculateBEIIfDataTypeIsSameForIncome();

                                                                }

                                                                updateTheBEIAmount(balance, expense, income);

                                                            }

                                                        }

                                                    }

                                                } else {

                                                    if (categorySharedPreferences.readCategoryName().equals("Expense")) {

                                                        if (amountTextView.getText().toString().contains(".")) {

                                                            float totalExpense = Float.parseFloat(amountTextView.getText().toString());
                                                            balance = String.valueOf(0 - totalExpense);
                                                            expense = String.valueOf(totalExpense);
                                                            income = "0.0";

                                                        } else {

                                                            long totalExpense = Long.parseLong(amountTextView.getText().toString());
                                                            balance = String.valueOf(-totalExpense);
                                                            expense = String.valueOf(totalExpense);
                                                            income = "0";

                                                        }

                                                        updateTheBEIAmount(balance, expense, income);

                                                    } else if (categorySharedPreferences.readCategoryName().equals("Income")) {

                                                        if (amountTextView.getText().toString().contains(".")) {

                                                            float totalIncome = Float.parseFloat(amountTextView.getText().toString());
                                                            balance = String.valueOf(totalIncome);
                                                            income = String.valueOf(totalIncome);
                                                            expense = "0.0";

                                                        } else {

                                                            long totalIncome = Long.parseLong(amountTextView.getText().toString());
                                                            balance = String.valueOf(totalIncome);
                                                            income = String.valueOf(totalIncome);
                                                            expense = "0";

                                                        }

                                                        updateTheBEIAmount(balance, expense, income);

                                                    }

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                                Log.e("database_error", error.getMessage());
                                            }
                                        });

                            }

                        }

                    }

                }

            }

        }

        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.addCardView:

                if (checkCategoryAndItemName()) {

                    addOperatorToTextView("+");

                }

                break;

            case R.id.cancelCardView:

                if (!amountTextView.getText().equals("0")) {

                    length = amountTextView.getText().length();

                    if (length == 1) {

                        text = "0";

                    } else {

                        text = "";
                        StringBuilder stringBuilderText = new StringBuilder();
                        amount = amountTextView.getText().toString().toCharArray();

                        for (in = 0; in < length - 1; in++) {

                            stringBuilderText = stringBuilderText.append(amount[in]);

                        }

                        text = stringBuilderText.toString();

                    }

                    amountTextView.setText(text);

                }

                break;

            case R.id.dotCardView:

                if (checkCategoryAndItemName()) {

                    text = "";

                    if (amountTextView.getText().equals("0")) {

                        text = "0.";
                        amountTextView.setText(text);

                    } else {

                        int operatorPos = 0, pos = 0;
                        amount = amountTextView.getText().toString().toCharArray();

                        for (in = 0; in < amount.length; in++) {

                            if (amount[in] == '+' || amount[in] == '-') {

                                operatorPos = in;
                                break;

                            }

                        }

                        if ((amount[operatorPos] == '+' || amount[operatorPos] == '-') && operatorPos == amount.length - 1) {

                            text = amountTextView.getText().toString() + "0.";
                            amountTextView.setText(text);

                        } else if ((amount[operatorPos] == '+' || amount[operatorPos] == '-') && operatorPos < amount.length - 1) {

                            StringBuilder secondNoStringBuilder = new StringBuilder();

                            for (in = operatorPos + 1; in < amount.length; in++) {

                                secondNoStringBuilder = secondNoStringBuilder.append(amount[in]);

                            }

                            String secondNo = secondNoStringBuilder.toString();
                            char[] secondNoArr = secondNo.toCharArray();

                            for (in = 0; in < secondNoArr.length; in++) {

                                if (secondNoArr[in] == '.') {

                                    pos = in;
                                    break;

                                }

                            }

                            if (secondNoArr[pos] != '.') {

                                text = amountTextView.getText() + ".";
                                amountTextView.setText(text);

                            }

                        } else {

                            for (in = 0; in < amount.length; in++) {

                                if (amount[in] == '.') {

                                    pos = in;
                                    break;

                                }

                            }

                            if (amount[pos] != '.') {

                                text = amountTextView.getText() + ".";
                                amountTextView.setText(text);

                            }

                        }

                    }

                }

                break;

            case R.id.eightCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("8");

                }

                break;

            case R.id.equalOrOkCardView:

                if (amountTextView.getText().toString().equals("0") || amountTextView.getText().toString().equals("0.")
                        || amountTextView.getText().toString().equals("0.0") || amountTextView.getText().toString().equals("0.00")
                        || amountTextView.getText().toString().equals("0.000")) {

                    amountTextView.setText("0");

                } else {

                    int operatorPos = 0;
                    text = amountTextView.getText().toString();
                    amount = text.toCharArray();

                    for (in = 0; in < amount.length; in++) {

                        if (amount[in] == '+' || amount[in] == '-') {

                            operatorPos = in;
                            break;

                        }

                    }

                    if (amount[operatorPos] == '+' || amount[operatorPos] == '-') {

                        checkIfDotIsPresentBeforeOrAfterOfOperator(amount, operatorPos);

                    }

                }

                break;

            case R.id.expenseCardView:

                if (categorySharedPreferences.readCategoryName().equals("Income")) {

                    itemNameTextView.setText(getResources().getString(R.string.add_item));

                    if (!amountTextView.getText().toString().equals("0")) {

                        amountTextView.setText("0");

                    }

                }

                categorySharedPreferences.writeCategoryName("Expense");
                category = "Category - " + categorySharedPreferences.readCategoryName();
                subcategoryTextView.setText(category);
                break;

            case R.id.fiveCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("5");

                }

                break;

            case R.id.fourCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("4");

                }

                break;

            case R.id.incomeCardView:

                if (categorySharedPreferences.readCategoryName().equals("Expense")) {

                    itemNameTextView.setText(getResources().getString(R.string.add_item));

                    if (!amountTextView.getText().toString().equals("0")) {

                        amountTextView.setText("0");

                    }

                }

                categorySharedPreferences.writeCategoryName("Income");
                category = "Category - " + categorySharedPreferences.readCategoryName();
                subcategoryTextView.setText(category);
                break;

            case R.id.nineCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("9");

                }

                break;

            case R.id.oneCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("1");

                }

                break;

            case R.id.sevenCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("7");

                }

                break;

            case R.id.sixCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("6");

                }

                break;

            case R.id.subcategoryCardView:

                if (categorySharedPreferences.readCategoryName().equals("")) {

                    showToast("Please, select Expense or Income.");

                } else if (categorySharedPreferences.readCategoryName().equals("Expense") || categorySharedPreferences.readCategoryName().equals("Income")) {

                    Intent intent = new Intent(this, AddItemCategoryActivity.class);
                    startActivityForResult(intent, 1);

                }

                break;

            case R.id.subtractCardView:

                if (checkCategoryAndItemName()) {

                    addOperatorToTextView("-");

                }

                break;

            case R.id.threeCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("3");

                }

                break;

            case R.id.todayCardView:
                datePickerDialog.show();
                break;

            case R.id.twoCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("2");

                }

                break;

            case R.id.zeroCardView:

                if (checkCategoryAndItemName()) {

                    addAmountToTextView("0");

                }

                break;

        }

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        month = month + 1;

        if (dayOfMonth != currentDay) {

            date = month + "/" + dayOfMonth;
            labelTextView.setText(date);
            todaysDateTextView.setText(String.valueOf(year));
            selectedDay = dayOfMonth;
            selectedMonth = month;
            selectedYear = year;

        } else {

            if (month != currentMonth) {

                date = month + "/" + dayOfMonth;
                labelTextView.setText(date);
                todaysDateTextView.setText(String.valueOf(year));
                selectedDay = dayOfMonth;
                selectedMonth = month;
                selectedYear = year;

            } else {

                if (year != currentYear) {

                    date = month + "/" + dayOfMonth;
                    labelTextView.setText(date);
                    todaysDateTextView.setText(String.valueOf(year));
                    selectedDay = dayOfMonth;
                    selectedMonth = month;
                    selectedYear = year;

                } else {

                    date = currentMonth + "/" + currentDay;
                    labelTextView.setText(getResources().getString(R.string.today));
                    todaysDateTextView.setText(date);
                    selectedDay = currentDay;
                    selectedMonth = currentMonth;
                    selectedYear = currentYear;

                }

            }

        }

    }
}
