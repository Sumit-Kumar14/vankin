package com.infinity.dev.vankin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinity.dev.vankin.Model.DifficultyLevel;

public class BottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener{

    OnOptionSelected mOnOptionSelected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        view.findViewById(R.id.li_easy).setOnClickListener(this);
        view.findViewById(R.id.li_medium).setOnClickListener(this);
        view.findViewById(R.id.li_hard).setOnClickListener(this);
        view.findViewById(R.id.li_challenge).setOnClickListener(this);
        return view;
    }

    public void setmOnOptionSelected(OnOptionSelected mOnOptionSelected) {
        this.mOnOptionSelected = mOnOptionSelected;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.li_easy:
                mOnOptionSelected.onOptionSelected(DifficultyLevel.EASY);
                break;
            case R.id.li_medium:
                mOnOptionSelected.onOptionSelected(DifficultyLevel.MEDIUM);
                break;
            case R.id.li_hard:
                mOnOptionSelected.onOptionSelected(DifficultyLevel.HARD);
                break;
            case R.id.li_challenge:
                mOnOptionSelected.onOptionSelected(DifficultyLevel.CHALLENGE);
                break;
        }
        this.dismiss();
    }

    public interface OnOptionSelected {
        void onOptionSelected(DifficultyLevel difficultyLevel);
    }
}