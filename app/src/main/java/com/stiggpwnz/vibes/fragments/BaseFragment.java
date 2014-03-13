package com.stiggpwnz.vibes.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.ButterKnife;
import lombok.Getter;
import mortar.Blueprint;
import mortar.Mortar;

public abstract class BaseFragment extends Fragment {
    @NotNull @Getter Context mortarContext;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @Nullable BaseFragment parentFragment = (BaseFragment) getParentFragment();
        @NotNull Context parentContext = getParentFragment() == null ?
                getActivity() :
                parentFragment.getMortarContext();
        @Nullable Blueprint bluePrint = getBluePrint();
        mortarContext = bluePrint != null ?
                Mortar.getScope(parentContext)
                        .requireChild(bluePrint)
                        .createContext(getActivity()) :
                parentContext;
    }

    @NotNull protected LayoutInflater getMortarInflater() {
        return LayoutInflater.from(mortarContext);
    }

    @Nullable protected Blueprint getBluePrint() { return null; }

    protected void configure(@NotNull ActionBar actionBar) { }

    @Nullable protected abstract View createView(LayoutInflater inflater, ViewGroup container);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @NotNull LayoutInflater mortarInflater = LayoutInflater.from(mortarContext);
        @Nullable View view = createView(mortarInflater, container);
        return view == null ?
                super.onCreateView(mortarInflater, container, savedInstanceState) :
                view;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            configure(getActivity().getActionBar());
        }
    }

    @Override public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public boolean onBackPressed() { return false; }
}
