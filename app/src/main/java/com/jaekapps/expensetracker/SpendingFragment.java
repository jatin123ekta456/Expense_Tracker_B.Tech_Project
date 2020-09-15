package com.jaekapps.expensetracker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class SpendingFragment extends Fragment implements View.OnClickListener {

    int currentMonth;
    private AppCompatButton showMoreButton;
    private ArrayList<Float> itemAmountPercentageList;
    private ArrayList<PieEntry> expenseItemList;
    private ArrayList<String> amountList, itemList, modifiedAmountList, modifiedItemList;
    private BEIAmount beiAmount;
    private CardView monthListCardView, yearListCardView;
    private DatabaseReference userDBReference;
    private int currentYear;
    private int[] colors;
    private PieChart spendingCategoryPieChart;
    private PieData expensesPieData;
    private PieDataSet expensesPieDataSet;
    private RecyclerView expenseItemRecyclerView, topCategoriesRecyclerView;
    private SpendingFragmentListener spendingFragmentListener;
    private String current_month, date, expense, month, year, userId;
    private TextView monthTitleTextView, natureOfSpendingAmountTextView, natureOfSpendingMonthTextView, spendingAmountTextView,
            spendingCurrentMonthTextView, yearTitleTextView;

    SpendingFragment(String month, String year) {

        this.month = month;
        this.year = year;
    }

    private ArrayList<String> makeAListOfTop5Categories(ArrayList<String> amountList) {

        ArrayList<String> tempList = new ArrayList<>();
        BigDecimal temp;
        BigDecimal[] amount = new BigDecimal[amountList.size()];

        for (int i = 0; i < amountList.size(); i++) {

            amount[i] = new BigDecimal(amountList.get(i));

        }

        for (int i = 0; i < amount.length; i++) {

            for (int j = i + 1; j < amount.length; j++) {

                if (amount[i].compareTo(amount[j]) < 0) {

                    temp = amount[j];
                    amount[j] = amount[i];
                    amount[i] = temp;

                }

            }

        }

        tempList.clear();

        for (int i = 0; i < 5; i++) {

            tempList.add(amount[i].toString());

        }

        return tempList;
    }

    private String convertToFullName(String month) {

        switch (month) {

            case "Jan":
                month = "January";
                break;

            case "Feb":
                month = "February";
                break;

            case "Mar":
                month = "March";
                break;

            case "Apr":
                month = "April";
                break;

            case "May":
                month = "May";
                break;

            case "June":
                month = "June";
                break;

            case "July":
                month = "July";
                break;

            case "Aug":
                month = "August";
                break;

            case "Sep":
                month = "September";
                break;

            case "Oct":
                month = "October";
                break;

            case "Nov":
                month = "November";
                break;

            case "Dec":
                month = "December";
                break;

        }

        return month;
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

    private String putComma(String amount) {

        char[] amt;
        int flag = 0, i, pos = 0;
        String new_amount = "";
        StringBuilder amountBuilder = new StringBuilder(new_amount);

        if (amount.contains(".")) {

            amt = amount.toCharArray();

            for (i = 0; i < amt.length; i++) {

                if (amt[i] == '.') {

                    pos = i;
                    break;

                } else {

                    amountBuilder.append(amt[i]);

                }

            }

            new_amount = amountBuilder.toString();
            amountBuilder = new StringBuilder();

            if (new_amount.length() >= 4) {

                amt = new_amount.toCharArray();
                char[] new_amt = amount.toCharArray();

                for (i = amt.length - 1; i >= 0; i--) {

                    if (flag < 3) {

                        amountBuilder.append(amt[i]);
                        flag++;

                    } else {

                        amountBuilder.append(',');
                        amountBuilder.append(amt[i]);
                        flag = 1;

                    }

                }

                amountBuilder.reverse();

                for (i = pos; i < new_amt.length; i++) {

                    amountBuilder.append(new_amt[i]);

                }

                new_amount = amountBuilder.toString();

            }

        } else {

            amt = amount.toCharArray();

            for (i = amt.length - 1; i >= 0; i--) {

                if (flag < 3) {

                    amountBuilder.append(amt[i]);
                    flag++;

                } else {

                    amountBuilder.append(",");
                    amountBuilder.append(amt[i]);
                    flag = 1;

                }

            }

            amountBuilder.reverse();
            new_amount = amountBuilder.toString();

        }

        return new_amount;
    }

    private void changeMonthNameIfMonthOrYearIsNotSame(String month_info) {

        natureOfSpendingMonthTextView.setText(month_info);
        spendingCurrentMonthTextView.setText(month_info);
    }

    private void initializeOnClickListener() {

        monthListCardView.setOnClickListener(this);
        showMoreButton.setOnClickListener(this);
        yearListCardView.setOnClickListener(this);
    }

    private void initializeViews(View view) {

        amountList = new ArrayList<>();
        beiAmount = new BEIAmount();
        Calendar calendar = Calendar.getInstance();
        colors = new int[] {
                getResources().getColor(R.color.amber, Objects.requireNonNull(getActivity()).getTheme()),
                getResources().getColor(R.color.blue, Objects.requireNonNull(getActivity()).getTheme()),
                getResources().getColor(R.color.deep_orange, Objects.requireNonNull(getActivity()).getTheme()),
                getResources().getColor(R.color.deep_purple, Objects.requireNonNull(getActivity()).getTheme()),
                getResources().getColor(R.color.green, Objects.requireNonNull(getActivity()).getTheme()),
        };
        currentMonth = calendar.get(Calendar.MONTH);
        currentMonth = currentMonth + 1;
        current_month = findMonth(currentMonth);
        currentYear = calendar.get(Calendar.YEAR);
        expenseItemList = new ArrayList<>();
        expenseItemRecyclerView = view.findViewById(R.id.expenseItemRecyclerView);
        itemAmountPercentageList = new ArrayList<>();
        itemList = new ArrayList<>();
        modifiedAmountList = new ArrayList<>();
        modifiedItemList = new ArrayList<>();
        monthListCardView = view.findViewById(R.id.monthListCardView);
        monthTitleTextView = view.findViewById(R.id.monthTitleTextView);
        natureOfSpendingAmountTextView = view.findViewById(R.id.natureOfSpendingAmountTextView);
        natureOfSpendingMonthTextView = view.findViewById(R.id.natureOfSpendingMonthTextView);
        showMoreButton = view.findViewById(R.id.showMoreButton);
        spendingAmountTextView = view.findViewById(R.id.spendingAmountTextView);
        spendingCategoryPieChart = view.findViewById(R.id.spendingCategoryPieChart);
        spendingCurrentMonthTextView = view.findViewById(R.id.spendingCurrentMonthTextView);
        topCategoriesRecyclerView = view.findViewById(R.id.topCategoriesRecyclerView);
        userDBReference = FirebaseDatabase.getInstance().getReference("User");
        UserIdConfigActivity userIdConfigActivity = new UserIdConfigActivity(view.getContext());
        userId = userIdConfigActivity.getUserID();
        yearListCardView = view.findViewById(R.id.yearListCardView);
        yearTitleTextView = view.findViewById(R.id.yearTitleTextView);
    }

    private void sortTheIndexList(ArrayList<Integer> indexList) {

        int temp;

        for (int i = 0; i < indexList.size(); i++) {

            for (int j = i + 1; j < indexList.size(); j++) {

                if (indexList.get(i) > indexList.get(j)) {

                    temp = indexList.get(i);
                    indexList.set(i, indexList.get(j));
                    indexList.set(j, temp);

                }

            }

        }
    }

    public interface SpendingFragmentListener {

        void showMonthListForSpending(String current_month, String current_year);
        void showYearListForSpending(String current_month, String current_year);
    }

    public void updateTheViews(final Context context, final String month, final String year) {

        String convertedMonth;
        this.month = month;
        this.year = year;
        monthTitleTextView.setText(month);
        yearTitleTextView.setText(year);

        if (currentYear == Integer.parseInt(year)) {

            if (current_month.equals(month)) {

                changeMonthNameIfMonthOrYearIsNotSame("THIS MONTH");

            } else {

                convertedMonth = convertToFullName(month);
                changeMonthNameIfMonthOrYearIsNotSame(convertedMonth + " " + year);

            }

        } else {

            convertedMonth = convertToFullName(month);
            changeMonthNameIfMonthOrYearIsNotSame(convertedMonth + " " + year);

        }

        userDBReference.child(userId)
                .child("BEIAmount")
                .child(year)
                .child(month)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            beiAmount = snapshot.getValue(BEIAmount.class);

                            if (beiAmount != null) {

                                beiAmount.setExpense(putComma(beiAmount.getExpense()));
                                expense = context.getResources()
                                        .getString(R.string.rupees) + beiAmount.getExpense();
                                natureOfSpendingAmountTextView.setText(expense);
                                spendingAmountTextView.setText(expense);

                            }

                        } else {

                            expense = context.getResources()
                                    .getString(R.string.rupees) + "0";
                            natureOfSpendingAmountTextView.setText(expense);
                            spendingAmountTextView.setText(expense);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Log.e("database_error", error.getMessage());
                    }
                });
        userDBReference.child(userId)
                .child("Expense_Category")
                .child(year)
                .child(month)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            if (snapshot.getChildrenCount() == 1) {

                                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {

                                    date  = dateSnapshot.getKey();

                                }

                                if (date != null) {

                                    amountList.clear();
                                    itemList.clear();

                                    for (DataSnapshot itemSnapshot : snapshot.child(date).getChildren()) {

                                        amountList.add(itemSnapshot.getValue(String.class));
                                        itemList.add(itemSnapshot.getKey());

                                    }

                                    if (amountList.size() == 1 && itemList.size() == 1) {

                                        spendingCategoryPieChart.animateY(1000, Easing.EaseInOutCubic);
                                        spendingCategoryPieChart.getDescription().setEnabled(false);
                                        amountList.set(0, context.getResources().getString(R.string.rupees) + putComma(amountList.get(0)));
                                        spendingCategoryPieChart.setCenterText(itemList.get(0) + "\n" + amountList.get(0) + "\n" + "100%");
                                        spendingCategoryPieChart.setDrawEntryLabels(false);
                                        spendingCategoryPieChart.setDrawHoleEnabled(true);
                                        spendingCategoryPieChart.setHoleRadius(65);
                                        spendingCategoryPieChart.setRotationEnabled(false);
                                        expenseItemList.clear();
                                        expenseItemList.add(new PieEntry(100f, itemList.get(0)));
                                        expensesPieDataSet = new PieDataSet(expenseItemList, "");
                                        expensesPieDataSet.setColors(colors);
                                        expensesPieDataSet.setDrawValues(false);
                                        expensesPieData = new PieData(expensesPieDataSet);
                                        spendingCategoryPieChart.setData(expensesPieData);
                                        expensesPieData.setValueTextColor(Color.WHITE);
                                        expensesPieData.setValueTextSize(10f);

                                    } else {

                                        BigDecimal amount_percentage;
                                        BigDecimal total_expense_amount = new BigDecimal(0);
                                        final float[] item_amount_percentage = new float[amountList.size()];

                                        for (int i = 0; i < amountList.size(); i++) {

                                            total_expense_amount = total_expense_amount.add(new BigDecimal(amountList.get(i)));

                                        }

                                        for (int i = 0; i < amountList.size(); i++) {

                                            amount_percentage = new BigDecimal(amountList.get(i)).divide(total_expense_amount, 3).multiply(new BigDecimal(100));
                                            item_amount_percentage[i] = Float.parseFloat(amount_percentage.toString());

                                        }

                                        itemAmountPercentageList.clear();

                                        for (float p : item_amount_percentage) {

                                            itemAmountPercentageList.add(p);

                                        }

                                        for (int i = 0; i < amountList.size(); i++) {

                                            amountList.set(i, context.getResources().getString(R.string.rupees) + putComma(amountList.get(i)));

                                        }

                                        spendingCategoryPieChart.animateY(1000, Easing.EaseInOutCubic);
                                        spendingCategoryPieChart.getDescription().setEnabled(false);
                                        spendingCategoryPieChart.getLegend().setWordWrapEnabled(true);
                                        spendingCategoryPieChart.setCenterText("All\n" + month + "\n" + context.getResources().getString(R.string.rupees) + putComma(String.valueOf(total_expense_amount)));
                                        spendingCategoryPieChart.setDrawEntryLabels(false);
                                        spendingCategoryPieChart.setDrawHoleEnabled(true);
                                        spendingCategoryPieChart.setHoleRadius(65);
                                        spendingCategoryPieChart.setRotationEnabled(false);
                                        expenseItemList.clear();

                                        for (int i = 0; i < itemList.size(); i++) {

                                            expenseItemList.add(new PieEntry(item_amount_percentage[i], itemList.get(i)));

                                        }

                                        expensesPieDataSet = new PieDataSet(expenseItemList, "");
                                        expensesPieDataSet.setColors(colors);
                                        expensesPieDataSet.setDrawValues(false);
                                        expensesPieData = new PieData(expensesPieDataSet);
                                        spendingCategoryPieChart.setData(expensesPieData);
                                        expensesPieData.setValueTextColor(Color.WHITE);
                                        expensesPieData.setValueTextSize(10f);
                                        final BigDecimal finalTotal_expense_amount = total_expense_amount;
                                        spendingCategoryPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                            @Override
                                            public void onValueSelected(Entry e, Highlight h) {

                                                int index = (int) h.getX();
                                                spendingCategoryPieChart.setCenterText(itemList.get(index) + "\n" + amountList.get(index) + "\n" + itemAmountPercentageList.get(index) + "%");
                                            }

                                            @Override
                                            public void onNothingSelected() {

                                                spendingCategoryPieChart.setCenterText("All\n" + month + context.getResources().getString(R.string.rupees) + putComma(finalTotal_expense_amount.toString()));
                                            }
                                        });

                                    }

                                }

                            } else {

                                amountList.clear();
                                final ArrayList<String> dateList, newAmountList, newItemList;
                                boolean alreadyAdded;
                                dateList = new ArrayList<>();
                                dateList.clear();
                                HashMap<String, String> itemHashMap = new HashMap<>();
                                itemHashMap.clear();
                                itemList.clear();
                                newAmountList = new ArrayList<>();
                                newAmountList.clear();
                                newItemList = new ArrayList<>();
                                newItemList.clear();
                                String item;

                                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {

                                    dateList.add(dateSnapshot.getKey());

                                }

                                for (int i = 0; i < dateList.size(); i++) {

                                    for (DataSnapshot itemSnapshot : snapshot.child(dateList.get(i)).getChildren()) {

                                        amountList.add(itemSnapshot.getValue(String.class));
                                        itemList.add(itemSnapshot.getKey());

                                    }

                                }

                                for (int i = 0; i < itemList.size(); i++) {

                                    alreadyAdded = false;
                                    BigDecimal amount = new BigDecimal(0);
                                    item = itemList.get(i);

                                    for (int j = 0; j < itemList.size(); j++) {

                                        if (!itemHashMap.isEmpty()) {

                                            if (itemHashMap.containsKey(item)) {

                                                alreadyAdded = true;
                                                break;

                                            } else {

                                                if (item.equals(itemList.get(j))) {

                                                    amount = amount.add(new BigDecimal(amountList.get(i)));

                                                }

                                            }

                                        } else {

                                            if (item.equals(itemList.get(j))) {

                                                amount = amount.add(new BigDecimal(amountList.get(i)));

                                            }

                                        }

                                    }

                                    if (!alreadyAdded) {

                                        itemHashMap.put(item, "Added");
                                        newAmountList.add(amount.toString());
                                        newItemList.add(item);

                                    }

                                }

                                ArrayList<Integer> indexList = new ArrayList<>();

                                if (newAmountList.size() > 5 && newItemList.size() > 5) {

                                    modifiedAmountList = makeAListOfTop5Categories(newAmountList);

                                    for (int i = 0; i < modifiedAmountList.size(); i++) {

                                        for (int j = 0; j < newAmountList.size(); j++) {

                                            if (modifiedAmountList.get(i).equals(newAmountList.get(j))) {

                                                indexList.add(j);

                                            }

                                        }

                                    }

                                    modifiedAmountList.clear();
                                    modifiedItemList.clear();
                                    sortTheIndexList(indexList);

                                    for (int i = 0; i < indexList.size(); i++) {

                                        modifiedAmountList.add(newAmountList.get(indexList.get(i)));
                                        modifiedItemList.add(newItemList.get(indexList.get(i)));

                                    }

                                } else {

                                    modifiedAmountList = newAmountList;
                                    modifiedItemList = newItemList;

                                }

                                BigDecimal amount_percentage;
                                BigDecimal total_expense_amount = new BigDecimal(0);
                                final float[] item_amount_percentage = new float[modifiedAmountList.size()];

                                for (int i = 0; i < modifiedAmountList.size(); i++) {

                                    total_expense_amount = total_expense_amount.add(new BigDecimal(modifiedAmountList.get(i)));

                                }

                                for (int i = 0; i < modifiedAmountList.size(); i++) {

                                    amount_percentage = new BigDecimal(modifiedAmountList.get(i)).divide(total_expense_amount, 3).multiply(new BigDecimal(100));
                                    item_amount_percentage[i] = Float.parseFloat(new DecimalFormat("##.##").format(amount_percentage));

                                }

                                itemAmountPercentageList.clear();

                                for (float p : item_amount_percentage) {

                                    itemAmountPercentageList.add(p);

                                }

                                for (int i = 0; i < modifiedAmountList.size(); i++) {

                                    modifiedAmountList.set(i, context.getResources().getString(R.string.rupees) + putComma(modifiedAmountList.get(i)));

                                }

                                spendingCategoryPieChart.animateY(1000, Easing.EaseInOutCubic);
                                spendingCategoryPieChart.getDescription().setEnabled(false);
                                spendingCategoryPieChart.getLegend().setWordWrapEnabled(true);
                                spendingCategoryPieChart.setCenterText("All\n" + month + "\n"  + context.getResources().getString(R.string.rupees) + putComma(String.valueOf(total_expense_amount)));
                                spendingCategoryPieChart.setDrawEntryLabels(false);
                                spendingCategoryPieChart.setDrawHoleEnabled(true);
                                spendingCategoryPieChart.setHoleRadius(65);
                                spendingCategoryPieChart.setRotationEnabled(false);
                                expenseItemList.clear();

                                for (int i = 0; i < modifiedItemList.size(); i++) {

                                    expenseItemList.add(new PieEntry(item_amount_percentage[i], modifiedItemList.get(i)));

                                }

                                expensesPieDataSet = new PieDataSet(expenseItemList, "");
                                expensesPieDataSet.setColors(colors);
                                expensesPieDataSet.setDrawValues(false);
                                expensesPieData = new PieData(expensesPieDataSet);
                                spendingCategoryPieChart.setData(expensesPieData);
                                expensesPieData.setValueTextColor(Color.WHITE);
                                expensesPieData.setValueTextSize(10f);
                                final BigDecimal finalTotal_expense_amount = total_expense_amount;
                                spendingCategoryPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, Highlight h) {

                                        int index = (int) h.getX();
                                        spendingCategoryPieChart.setCenterText(modifiedItemList.get(index) + "\n" + modifiedAmountList.get(index) + "\n" + itemAmountPercentageList.get(index) + "%");
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                        spendingCategoryPieChart.setCenterText("All\n" + month + "\n"  + context.getResources().getString(R.string.rupees) + putComma(finalTotal_expense_amount.toString()));
                                    }
                                });

                            }

                        } else {

                            spendingCategoryPieChart.setData(null);
                            spendingCategoryPieChart.invalidate();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Log.e("database_error", error.getMessage());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.spending_fragment, container, false);
        initializeViews(view);
        initializeOnClickListener();
        updateTheViews(view.getContext(), month, year);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {

            spendingFragmentListener = (SpendingFragmentListener) context;

        } catch (Exception e) {

            throw new ClassCastException(context.toString() + " must implement SpendingFragmentListener!");

        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.monthListCardView:
                spendingFragmentListener.showMonthListForSpending(month, year);
                break;

            case R.id.showMoreButton:
                break;

            case R.id.yearListCardView:
                spendingFragmentListener.showYearListForSpending(month, year);
                break;

        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        spendingFragmentListener = null;
    }
}
