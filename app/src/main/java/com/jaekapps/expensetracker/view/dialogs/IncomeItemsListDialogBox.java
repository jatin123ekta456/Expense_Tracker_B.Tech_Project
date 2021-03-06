package com.jaekapps.expensetracker.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaekapps.expensetracker.R;
import com.jaekapps.expensetracker.view.adapters.MoreIncomeRecyclerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class IncomeItemsListDialogBox extends DialogFragment {

    private final ArrayList<Integer> itemIconList;
    private final ArrayList<String> amountList, itemList;
    private final int[] colors;

    public IncomeItemsListDialogBox(ArrayList<Integer> itemIconList, ArrayList<String> amountList, ArrayList<String> itemList, int[] colors) {

        this.amountList = amountList;
        this.colors = colors;
        this.itemIconList = itemIconList;
        this.itemList = itemList;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_income_items_list, null);
        RecyclerView incomeItemsListRecyclerView = view.findViewById(R.id.incomeItemsListRecyclerView);
        incomeItemsListRecyclerView.setHasFixedSize(true);
        incomeItemsListRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        for (int i = 0; i < amountList.size(); i++) {

            if (!amountList.get(i).contains(view.getContext().getResources()
                    .getString(R.string.rupees))) {

                amountList.set(i, view.getContext().getResources()
                        .getString(R.string.rupees) + " " + putComma(amountList.get(i)));

            }

        }

        MoreIncomeRecyclerAdapter moreExpensesRecyclerAdapter = new MoreIncomeRecyclerAdapter(
                itemIconList,
                amountList,
                itemList,
                view.getContext(),
                colors
        );
        incomeItemsListRecyclerView.setAdapter(moreExpensesRecyclerAdapter);
        builder.setView(view);
        builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }
}
