/*
 * Copyright (C) 2015 Jorge Ruesga
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ruesga.android.wallpapers.photophase.preferences;

import com.ruesga.android.wallpapers.photophase.R;
import com.ruesga.android.wallpapers.photophase.adapters.LivePreviewAdapter.LivePreviewCallback;
import com.ruesga.android.wallpapers.photophase.effects.Effects;

import java.util.HashSet;
import java.util.Set;

import com.ruesga.android.wallpapers.photophase.preferences.PreferencesProvider.Preferences.General;
import com.ruesga.android.wallpapers.photophase.transitions.Transitions;

public class LiveEffectsPreviewFragment extends LivePreviewFragment {

    private final LivePreviewCallback mCallback = new LivePreviewCallback() {
        @Override
        public Set<String> getSelectedEntries() {
            Effects.EFFECTS[] effects = General.Effects.toEFFECTS(
                    General.Effects.getSelectedEffects());
            Set<String> set = new HashSet<>(effects.length);
            for (Effects.EFFECTS effect : effects) {
                set.add(String.valueOf(effect.mId));
            }
            return set;
        }

        @Override
        public void setSelectedEntries(Set<String> entries) {
            General.Effects.setSelectedEffects(getActivity(), entries);
        }

        @Override
        public Transitions.TRANSITIONS getTransitionForPosition(int position) {
            return Transitions.TRANSITIONS.NO_TRANSITION;
        }

        @Override
        public Effects.EFFECTS getEffectForPosition(int position) {
            String[] entries = getActivity().getResources().getStringArray(getEntries());
            return Effects.EFFECTS.fromId(Integer.valueOf(entries[position]));
        }
    };

    @Override
    public int getLabels() {
        return R.array.effects_labels;
    }

    @Override
    public int getEntries() {
        return R.array.effects_values;
    }

    @Override
    public LivePreviewCallback getLivePreviewCallback() {
        return mCallback;
    }
}