package com.thebluealliance.androidclient.views.breakdowns;

import static com.thebluealliance.androidclient.views.breakdowns.MatchBreakdownHelper.getIntDefault;
import static com.thebluealliance.androidclient.views.breakdowns.MatchBreakdownHelper.getIntDefaultValue;
import static com.thebluealliance.androidclient.views.breakdowns.MatchBreakdownHelper.teamNumberFromKey;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.google.gson.JsonObject;
import com.thebluealliance.androidclient.R;
import com.thebluealliance.androidclient.types.MatchType;
import com.thebluealliance.api.model.IMatchAlliancesContainer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MatchBreakdownView2015 extends AbstractMatchBreakdownView {

    @BindView(R.id.breakdown2015_container)                  GridLayout breakdownContainer;

    @BindView(R.id.breakdown_red1)                           TextView red1;
    @BindView(R.id.breakdown_blue1)                          TextView blue1;
    @BindView(R.id.breakdown_red2)                           TextView red2;
    @BindView(R.id.breakdown_blue2)                          TextView blue2;
    @BindView(R.id.breakdown_red3)                           TextView red3;
    @BindView(R.id.breakdown_blue3)                          TextView blue3;

    @BindView(R.id.breakdown2015_auto_tote_points_red)       TextView autoTotePointsRed;
    @BindView(R.id.breakdown2015_auto_tote_points_blue)      TextView autoTotePointsBlue;
    @BindView(R.id.breakdown2015_auto_container_points_red)  TextView autoContainerPointsRed;
    @BindView(R.id.breakdown2015_auto_container_points_blue) TextView autoContainerPointsBlue;
    @BindView(R.id.breakdown2015_auto_robot_points_red)      TextView autoRobotPointsRed;
    @BindView(R.id.breakdown2015_auto_robot_points_blue)     TextView autoRobotPointsBlue;
    @BindView(R.id.breakdown_auto_total_red)                 TextView autoTotalRed;
    @BindView(R.id.breakdown_auto_total_blue)                TextView autoTotalBlue;

    @BindView(R.id.breakdown2015_tote_points_red)            TextView totePointsRed;
    @BindView(R.id.breakdown2015_tote_points_blue)           TextView totePointsBlue;
    @BindView(R.id.breakdown2015_container_points_red)       TextView containerPointsRed;
    @BindView(R.id.breakdown2015_container_points_blue)      TextView containerPointsBlue;
    @BindView(R.id.breakdown2015_litter_points_red)          TextView litterPointsRed;
    @BindView(R.id.breakdown2015_litter_points_blue)         TextView litterPointsBlue;
    @BindView(R.id.breakdown_teleop_total_red)               TextView teleopTotalRed;
    @BindView(R.id.breakdown_teleop_total_blue)              TextView teleopTotalBlue;

    @BindView(R.id.breakdown2015_coop_points_red)            TextView coopPointsRed;
    @BindView(R.id.breakdown2015_coop_points_blue)           TextView coopPointsBlue;
    @BindView(R.id.breakdown_fouls_red)                      TextView foulsRed;
    @BindView(R.id.breakdown_fouls_blue)                     TextView foulsBlue;
    @BindView(R.id.breakdown_adjust_red)                     TextView adjustRed;
    @BindView(R.id.breakdown_adjust_blue)                    TextView adjustBlue;
    @BindView(R.id.breakdown_total_red)                      TextView totalRed;
    @BindView(R.id.breakdown_total_blue)                     TextView totalBlue;

    @BindView(R.id.breakdown2015_auto_totes_icon_red)        ImageView autoTotesIconRed;
    @BindView(R.id.breakdown2015_auto_totes_icon_blue)       ImageView autoTotesIconBlue;
    @BindView(R.id.breakdown2015_auto_containers_icon_red)   ImageView autoContainersIconRed;
    @BindView(R.id.breakdown2015_auto_containers_icon_blue)  ImageView autoContainersIconBlue;
    @BindView(R.id.breakdown2015_auto_robots_icon_red)       ImageView autoRobotsIconRed;
    @BindView(R.id.breakdown2015_auto_robots_icon_blue)      ImageView autoRobotsIconBlue;
    @BindView(R.id.breakdown2015_coop_icon_red)              ImageView coopIconRed;
    @BindView(R.id.breakdown2015_coop_icon_blue)             ImageView coopIconBlue;

    private static final int ROBOT_SET_POINTS = 4;
    private static final int TOTE_SET_POINTS = 6;
    private static final int CONTAINER_SET_POINTS = 8;
    private static final int TOTE_STACK_POINTS = 20;
    private static final int COOP_SET_POINTS = 20;
    private static final int COOP_STACK_POINTS = 40;

    public MatchBreakdownView2015(Context context) {
        super(context);
    }

    public MatchBreakdownView2015(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MatchBreakdownView2015(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void init() {
        // Inflate the layout
        LayoutInflater.from(getContext()).inflate(R.layout.match_breakdown_2015, this, true);

        ButterKnife.bind(this);
    }

    public boolean initWithData(MatchType matchType,
                                String winningAlliance,
                                IMatchAlliancesContainer allianceData,
                                JsonObject scoreData) {
        if (scoreData == null || scoreData.entrySet().isEmpty()
            || allianceData == null || allianceData.getRed() == null || allianceData.getBlue() == null) {
            breakdownContainer.setVisibility(GONE);
            return false;
        }

        List<String> redTeams = allianceData.getRed().getTeamKeys();
        List<String> blueTeams = allianceData.getBlue().getTeamKeys();
        JsonObject redData = scoreData.get("red").getAsJsonObject();
        JsonObject blueData = scoreData.get("blue").getAsJsonObject();

        red1.setText(teamNumberFromKey(redTeams.get(0)));
        red2.setText(teamNumberFromKey(redTeams.get(1)));
        red3.setText(teamNumberFromKey(redTeams.get(2)));

        blue1.setText(teamNumberFromKey(blueTeams.get(0)));
        blue2.setText(teamNumberFromKey(blueTeams.get(1)));
        blue3.setText(teamNumberFromKey(blueTeams.get(2)));

        /* Auto Totes */
        if (redData.get("tote_set").getAsBoolean()) {
            autoTotePointsRed.setText(getContext().getString(R.string.breakdown_addition_format, TOTE_SET_POINTS));
            autoTotesIconRed.setImageResource(R.drawable.ic_more_horiz_black_24dp);
        } else if (redData.get("tote_stack").getAsBoolean()) {
            autoTotePointsRed.setText(getContext().getString(R.string.breakdown_addition_format, TOTE_STACK_POINTS));
            autoTotesIconRed.setImageResource(R.drawable.ic_menu_black_24dp);
        } else {
            autoTotePointsRed.setVisibility(GONE);
            autoTotesIconRed.setImageResource(R.drawable.ic_clear_black_24dp);
        }
        if (blueData.get("tote_set").getAsBoolean()) {
            autoTotePointsBlue.setText(getContext().getString(R.string.breakdown_addition_format, TOTE_SET_POINTS));
            autoTotesIconBlue.setImageResource(R.drawable.ic_more_horiz_black_24dp);
        } else if (blueData.get("tote_stack").getAsBoolean()) {
            autoTotePointsBlue.setText(getContext().getString(R.string.breakdown_addition_format, TOTE_STACK_POINTS));
            autoTotesIconBlue.setImageResource(R.drawable.ic_menu_black_24dp);
        } else {
            autoTotePointsBlue.setVisibility(GONE);
            autoTotesIconBlue.setImageResource(R.drawable.ic_clear_black_24dp);
        }

        /* Auto Containers */
        if (redData.get("container_set").getAsBoolean()) {
            autoContainerPointsRed.setText(getContext().getString(R.string.breakdown_addition_format, CONTAINER_SET_POINTS));
            autoContainersIconRed.setImageResource(R.drawable.ic_more_horiz_black_24dp);
        } else {
            autoContainerPointsRed.setVisibility(GONE);
            autoContainersIconRed.setImageResource(R.drawable.ic_clear_black_24dp);
        }
        if (blueData.get("container_set").getAsBoolean()) {
            autoContainerPointsBlue.setText(getContext().getString(R.string.breakdown_addition_format, CONTAINER_SET_POINTS));
            autoContainersIconBlue.setImageResource(R.drawable.ic_more_horiz_black_24dp);
        } else {
            autoContainerPointsBlue.setVisibility(GONE);
            autoContainersIconBlue.setImageResource(R.drawable.ic_clear_black_24dp);
        }

        /* Auto Robots */
        if (redData.get("robot_set").getAsBoolean()) {
            autoRobotPointsRed.setText(getContext().getString(R.string.breakdown_addition_format,  ROBOT_SET_POINTS));
            autoRobotsIconRed.setImageResource(R.drawable.ic_more_horiz_black_24dp);
        } else {
            autoRobotPointsRed.setVisibility(GONE);
            autoRobotsIconRed.setImageResource(R.drawable.ic_clear_black_24dp);
        }
        if (blueData.get("robot_set").getAsBoolean()) {
            autoRobotPointsBlue.setText(getContext().getString(R.string.breakdown_addition_format, ROBOT_SET_POINTS));
            autoRobotsIconBlue.setImageResource(R.drawable.ic_more_horiz_black_24dp);
        } else {
            autoRobotPointsBlue.setVisibility(GONE);
            autoRobotsIconBlue.setImageResource(R.drawable.ic_clear_black_24dp);
        }

        /* Auto Points */
        autoTotalRed.setText(getIntDefault(redData, "auto_points"));
        autoTotalBlue.setText(getIntDefault(blueData, "auto_points"));

        /* Teleop Points */
        int redTote = getIntDefaultValue(redData, "tote_points");
        int blueTote = getIntDefaultValue(blueData, "tote_points");
        int redContainer = getIntDefaultValue(redData, "container_points");
        int blueContainer = getIntDefaultValue(blueData, "container_points");
        int redLitter = getIntDefaultValue(redData, "litter_points");
        int blueLitter = getIntDefaultValue(blueData, "litter_points");
        totePointsRed.setText(getIntDefault(redData, "tote_points"));
        totePointsBlue.setText(getIntDefault(blueData, "tote_points"));
        containerPointsRed.setText(getIntDefault(redData, "container_points"));
        containerPointsBlue.setText(getIntDefault(blueData, "container_points"));
        litterPointsRed.setText(getIntDefault(redData, "litter_points"));
        litterPointsBlue.setText(getIntDefault(blueData, "litter_points"));

        teleopTotalRed.setText(getResources().getString(R.string.breakdown_number_format,
                                                        (redTote + redContainer + redLitter)));
        teleopTotalBlue.setText(getResources().getString(R.string.breakdown_number_format,
                                                         (blueTote + blueContainer + blueLitter)));

        /* Coop */
        if (matchType.isPlayoff()) {
            coopIconRed.setVisibility(GONE);
            coopIconBlue.setVisibility(GONE);
            coopPointsRed.setVisibility(GONE);
            coopPointsBlue.setVisibility(GONE);
            coopPointsRed.setText("0");
            coopPointsBlue.setText("0");
        } else if (scoreData.get("coopertition").getAsString().equals("Stack")){
            coopIconRed.setImageResource(R.drawable.ic_menu_black_24dp);
            coopIconBlue.setImageResource(R.drawable.ic_menu_black_24dp);
            coopPointsRed.setText(getContext().getString(R.string.breakdown_addition_format, COOP_STACK_POINTS));
            coopPointsBlue.setText(getContext().getString(R.string.breakdown_addition_format, COOP_STACK_POINTS));
        } else if (scoreData.get("coopertition").getAsString().equals("Set")) {
            coopIconRed.setImageResource(R.drawable.ic_more_horiz_black_24dp);
            coopIconBlue.setImageResource(R.drawable.ic_more_horiz_black_24dp);
            coopPointsRed.setText(getContext().getString(R.string.breakdown_addition_format, COOP_SET_POINTS));
            coopPointsBlue.setText(getContext().getString(R.string.breakdown_addition_format, COOP_SET_POINTS));
        } else {
            coopIconRed.setImageResource(R.drawable.ic_clear_black_24dp);
            coopIconBlue.setImageResource(R.drawable.ic_clear_black_24dp);
        }

        /* Other Values */
        foulsRed.setText(getResources().getString(R.string.breakdown_foul_format_subtract,
                                                  getIntDefaultValue(redData, "foul_points")));
        foulsBlue.setText(getResources().getString(R.string.breakdown_foul_format_subtract,
                                                   getIntDefaultValue(blueData, "foul_points")));
        adjustRed.setText(getIntDefault(redData, "adjust_points"));
        adjustBlue.setText(getIntDefault(blueData, "adjust_points"));
        totalRed.setText(getIntDefault(redData, "total_points"));
        totalBlue.setText(getIntDefault(blueData, "total_points"));

        return true;
    }
}
