package com.dash.dietdash;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dash.adapter.NutritionListAdapter;
import com.dash.handler.Calculation;
import com.dash.handler.DataBaseHelper;
import com.dash.model.Alternative;
import com.dash.model.Calory;
import com.dash.model.User;

import java.util.ArrayList;

import static android.R.attr.defaultValue;
import static android.content.Context.MODE_PRIVATE;

public class DetailNutrition extends Fragment {

    private ListView listView;
    private ArrayList<Calory> listCal;
    private int day, category;
    private View header;
    private DataBaseHelper dbHelper;
    private String userEmail, date;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.activity_detail_nutrition, null);
        header = (View) inflater.inflate(R.layout.list_view_header, null);
        Bundle bundle = this.getArguments();
        day = bundle.getInt("day", defaultValue);
        date = bundle.getString("date", "");
        getAllWidgets(rootView);
        return rootView;
    }

    private void getAllWidgets(View view) {
        listView = (ListView) view.findViewById(R.id.listNutrition);
        SharedPreferences userDetails = getActivity().getSharedPreferences("dietPrefs", MODE_PRIVATE);
        userEmail = userDetails.getString("emailKey", "");

        dbHelper = new DataBaseHelper(getActivity());
        dbHelper.openDataBase();

        // Get User Data
        User user = dbHelper.getOneUser(userEmail);

        // Calculate AKG
        Calculation c = new Calculation();
        double BMR = c.calculateBMR(user.getGender(), user.getAge(), user.getWeight());
        double AKG = c.calculateAKG(user.getGender(), BMR, user.getActivity());

        if (AKG <= 2000) {
            category = 1800;
        } else if (AKG <= 2200) {
            category = 2200;
        } else {
            category = 2500;
        }

        listCal = dbHelper.getCalory(category, day);
        NutritionListAdapter listViewAdapter = new NutritionListAdapter(DetailSchedule.getInstance(), listCal);
        ((TextView) header.findViewById(R.id.txtHeader)).setText("KANDUNGAN NUTRISI");
        listView.addHeaderView(header);
        listView.setAdapter(listViewAdapter);

        Button btn = (Button) rootView.findViewById(R.id.btnCatat);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                ArrayList<String> list = dbHelper.getAlternatifDate(userEmail);
                boolean status = true;
                for (int i = 0; i < list.size(); i++) {

                    if (list.get(i).equals(date)) {
                        status = false;
                    }
                }
                if (status == true) {
                    Alternative a = new Alternative(userEmail, category, day, date);
                    dbHelper.addAlternative(a);
                    Toast.makeText(getActivity(), "Alternatif menu " + day + " telah disimpan!", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(getActivity(), "Penyimpanan gagal\nAnda tidak dapat mencatat lebih dari 1 alternatif dalam sehari!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
