package org.worldbank.transport.driver.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.azavea.androidvalidatedforms.tasks.ValidationTask;

import org.worldbank.transport.driver.R;
import org.worldbank.transport.driver.utilities.DriverUtilities;
import org.worldbank.transport.driver.utilities.RecordFormSectionManager;


/**
 * Form for a section that does not contain multiple elements.
 *
 * Created by kathrynkillebrew on 12/31/15.
 */
public class RecordFormSectionActivity extends RecordFormActivity {

    private static final String LOG_LABEL = "FormSectionActivity";

    @Override
    protected Object getModelObject() {
        return RecordFormSectionManager.getOrCreateSectionObject(sectionField, sectionClass, currentlyEditing);
    }

    /**
     * Helper to build a layout with previous/next/save buttons.
     *
     * @return View with buttons with handlers added to it
     */
    @Override
    public RelativeLayout buildButtonBar() {
        // reference to this, for use in button actions (validation task makes weak ref)
        final RecordFormActivity thisActivity = this;

        // put buttons in a relative layout for positioning on right or left
        RelativeLayout buttonBar = new RelativeLayout(this);
        buttonBar.setId(R.id.record_button_bar_id);
        buttonBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // add 'previous' button
        havePrevious = true;
        Button backBtn = new Button(this);
        RelativeLayout.LayoutParams backBtnLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (!DriverUtilities.localeIsRTL()) {
            backBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            backBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        backBtn.setLayoutParams(backBtnLayoutParams);

        backBtn.setId(R.id.record_back_button_id);
        backBtn.setText(getText(R.string.record_previous_button));
        buttonBar.addView(backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_LABEL, "Back button clicked");
                if (!thisActivity.isFormReady()) {
                    return; // cannot run validation until form finishes loading
                }
                // set this to let callback know next action to take
                goPrevious = true;
                goExit = false;
                new ValidationTask(thisActivity).execute();
            }
        });

        // add next/save button
        Button goBtn = new Button(this);
        RelativeLayout.LayoutParams goBtnLayoutParams = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        if (DriverUtilities.localeIsRTL()) {
            goBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            goBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        goBtn.setLayoutParams(goBtnLayoutParams);

        goBtn.setId(R.id.record_save_button_id);

        if (RecordFormSectionManager.sectionHasNext(sectionId)) {
            // add 'next' button
            haveNext = true;
            goBtn.setText(getString(R.string.record_next_button));

        } else {
            haveNext = false;
            // add 'save' button
            goBtn.setText(getString(R.string.record_save_button));
        }

        buttonBar.addView(goBtn);

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_LABEL, "Next/save button clicked");
                if (!thisActivity.isFormReady()) {
                    return; // cannot run validation until form finishes loading
                }

                goPrevious = false;
                goExit = false;
                new ValidationTask(thisActivity).execute();
            }
        });

        return buttonBar;
    }

    /**
     * Hardware back button pressed. Validate section before proceeding.
     */
    @Override
    public void onBackPressed() {
        if (!this.isFormReady()) {
            return; // cannot run validation until form finishes loading
        }
        goPrevious = true;
        goExit = false;
        new ValidationTask(this).execute();
    }

    @Override
    public void proceed() {
        if (goExit) {
            RecordFormSectionManager.saveAndExit(app, this);
            return;
        }

        int goToSectionId = sectionId;

        if (goPrevious) {
            goToSectionId--;
        } else if (haveNext) {
            Log.d(LOG_LABEL, "Proceed to next section now");
            goToSectionId++;
        } else {
            Log.d(LOG_LABEL, "Saving completed form");
            saveAndExit();
            return;
        }

        Log.d(LOG_LABEL, "Going to section #" + String.valueOf(goToSectionId));
        Intent intent = new Intent(this,
                RecordFormSectionManager.getActivityClassForSection(goToSectionId));

        intent.putExtra(RecordFormActivity.SECTION_ID, goToSectionId);
        startActivity(intent);

        if (goPrevious) {
            finish();
        }
    }
}

