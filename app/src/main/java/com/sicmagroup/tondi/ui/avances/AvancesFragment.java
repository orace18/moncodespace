package com.sicmagroup.tondi.ui.avances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sicmagroup.tondi.databinding.FragmentAvancesBinding;

public class AvancesFragment extends Fragment {

    private FragmentAvancesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AvancesViewModel avancesViewModel =
                new ViewModelProvider(this).get(AvancesViewModel.class);

        binding = FragmentAvancesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}