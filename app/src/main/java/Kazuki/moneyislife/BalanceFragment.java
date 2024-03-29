package Kazuki.moneyislife;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import Kazuki.moneyislife.api.Api;
import Kazuki.moneyislife.api.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BalanceFragment extends Fragment {

    private TextView balance;
    private TextView income;
    private TextView expenses;
    private DiagramView diagram;
    private int incomeSum;
    private int expensesSum;
    private App app;
    private Api api;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) Objects.requireNonNull(getActivity()).getApplication();
        api = app.getApi();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.balance_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        balance = view.findViewById(R.id.balance);
        income = view.findViewById(R.id.income);
        expenses = view.findViewById(R.id.expenses);
        diagram = view.findViewById(R.id.diagram);
        loadDate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDate();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            loadDate();
        }
    }

    private void setIncome(List<Item> incomes) {
        new Thread(() -> {
            incomeSum = 0;
            for (int i = 0; i < incomes.size(); i++) {
                incomeSum += Integer.parseInt(incomes.get(i).getPrice());
            }
        }).start();
    }

    private void setExpenses(List<Item> expenses) {
        new Thread(() -> {
            expensesSum = 0;
            for (int i = 0; i < expenses.size(); i++) {
                expensesSum += Integer.parseInt(expenses.get(i).getPrice());
            }
        }).start();

    }

    private void loadDate() {
        Call<List<Item>> call1 = api.getItems(Item.TYPE_INCOMES);
        call1.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NotNull Call<List<Item>> call, @NotNull Response<List<Item>> response) {
                List<Item> res = response.body();
                if (res != null)
                    setIncome(res);
                income.setText(getString(R.string.rub, incomeSum));
            }

            @Override
            public void onFailure(@NotNull Call<List<Item>> call, @NotNull Throwable t) {
            }
        });
        Call<List<Item>> call2 = api.getItems(Item.TYPE_EXPENSES);
        call2.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NotNull Call<List<Item>> call, @NotNull Response<List<Item>> response) {
                List<Item> res = response.body();
                if (res != null)
                    setExpenses(res);

                expenses.setText(getString(R.string.rub, expensesSum));
            }

            @Override
            public void onFailure(@NotNull Call<List<Item>> call, @NotNull Throwable t) {

            }
        });
        balance.setText(getString(R.string.rub, incomeSum - expensesSum));
        diagram.update(incomeSum, expensesSum);

    }

}
