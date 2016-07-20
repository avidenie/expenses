/*
 * Copyright © 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.ui.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;

public class AppBarHelper implements AppBarLayout.OnOffsetChangedListener {

    public interface Callback {
        void onStateChanged(AppBarLayout appBarLayout, State state);
    }

    public enum State {
        EXPANDED,
        COLLAPSED,
        UNDETERMINED
    }

    private Callback mCallback;

    private AppBarLayout mAppBarLayout;

    private State mState = State.UNDETERMINED;

    public AppBarHelper() {
    }

    public AppBarHelper(@NonNull Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            if (mState != State.EXPANDED) {
                if (mCallback != null) {
                    mCallback.onStateChanged(appBarLayout, State.EXPANDED);
                }
            }
            mState = State.EXPANDED;
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            if (mState != State.COLLAPSED) {
                if (mCallback != null) {
                    mCallback.onStateChanged(appBarLayout, State.COLLAPSED);
                }
            }
            mState = State.COLLAPSED;
        } else {
            if (mState != State.UNDETERMINED) {
                if (mCallback != null) {
                    mCallback.onStateChanged(appBarLayout, State.UNDETERMINED);
                }
            }
            mState = State.UNDETERMINED;
        }
    }

    public State getState() {
        return mState;
    }

    public void attachToAppBar(@Nullable AppBarLayout appBarLayout) {

        if (mAppBarLayout == appBarLayout) {
            return; // nothing to do
        }

        if (mAppBarLayout != null) {
            destroyCallbacks();
        }

        mAppBarLayout = appBarLayout;

        if (mAppBarLayout != null) {
            setupCallbacks();
        }
    }

    public void setExpanded(boolean expanded) {
        mAppBarLayout.setExpanded(expanded);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        mAppBarLayout.setExpanded(expanded, animate);
    }

    private void setupCallbacks() {
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    private void destroyCallbacks() {
        mAppBarLayout.removeOnOffsetChangedListener(this);
    }
}
