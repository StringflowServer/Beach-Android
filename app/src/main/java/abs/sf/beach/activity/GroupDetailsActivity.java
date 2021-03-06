package abs.sf.beach.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import abs.ixi.client.Platform;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.ChatRoom;
import abs.sf.beach.adapter.GroupDetailsAdapter;
import abs.sf.beach.android.R;
import abs.sf.beach.utils.AndroidUtils;
import abs.sf.beach.utils.CommonConstants;
import abs.sf.beach.utils.OnRefreshViewListener;
import abs.sf.client.android.managers.AndroidUserManager;


public class GroupDetailsActivity extends StringflowActivity implements OnRefreshViewListener {
    private RecyclerView recyclerView;
    private ImageView ivBack, ivNext, ivContactImage, ivEdit;
    private TextView tvHeader, tvParticipants, tvAddParticipants, tvNoLongerMember, tvGroupType;
    public TextView tvGroupName;
    private CardView cvExitGroup, cvReportSpam, cvDeleteGroup;
    private FrameLayout addParticipantContainer;
    private ChatRoom chatRoom;
    private List<ChatRoom.ChatRoomMember> memberList;
    private JID roomJID;
    private boolean isGroupMember;
    private GroupDetailsAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        initView();
        initOnclickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.roomJID = (JID) getIntent().getSerializableExtra(CommonConstants.JID);

        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        this.chatRoom = userManager.getChatRoomDetails(this.roomJID);

        this.memberList = new ArrayList<>(this.chatRoom.getMembers());
        this.isGroupMember = this.chatRoom.isRoomMember(Platform.getInstance().getUserJID());

        setChatAdapter();
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivNext = (ImageView) findViewById(R.id.ivNext);

        ivEdit = (ImageView) findViewById(R.id.ivEditName);
        ivEdit.setVisibility(View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        tvParticipants = (TextView) findViewById(R.id.tvParticipants);
        cvReportSpam = (CardView) findViewById(R.id.cvReportSpam);
        addParticipantContainer = (FrameLayout) findViewById(R.id.addParticipantContainer);
        ivNext.setVisibility(View.INVISIBLE);
        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        tvGroupType = (TextView) findViewById(R.id.tvGroupType);
        ivContactImage = (ImageView) findViewById(R.id.ivContactImage);
        tvHeader.setVisibility(View.GONE);

        tvNoLongerMember = (TextView) findViewById(R.id.tvNoLonger);
        tvNoLongerMember.setVisibility(View.GONE);

        tvAddParticipants = (TextView) findViewById(R.id.tvAddParticipants);
        tvAddParticipants.setVisibility(View.GONE);

        cvExitGroup = (CardView) findViewById(R.id.cvExitGroup);
        cvExitGroup.setVisibility(View.GONE);

        cvDeleteGroup = (CardView) findViewById(R.id.cvDeleteGroup);
        cvDeleteGroup.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

    }


    private void setChatAdapter() {
        tvGroupType.setText(chatRoom.getAccessMode().val());
        tvParticipants.setText(memberList.size() + " Participants");
        tvGroupName.setText(this.chatRoom.getSubject());

        ChatRoom.ChatRoomMember loggedInMember = null;

        if(isGroupMember) {
            loggedInMember = this.chatRoom.getMember(Platform.getInstance().getUserJID());

            if (loggedInMember.getAffiliation() == ChatRoom.Affiliation.OWNER || loggedInMember.getAffiliation() == ChatRoom.Affiliation.ADMIN ) {
                tvAddParticipants.setVisibility(View.VISIBLE);
            }
            else{
                tvAddParticipants.setVisibility(View.GONE);
                tvNoLongerMember.setVisibility(View.VISIBLE);
            }

            if (loggedInMember.getAffiliation() == ChatRoom.Affiliation.OWNER) {
                cvDeleteGroup.setVisibility(View.VISIBLE);
            }
            ivEdit.setVisibility(View.VISIBLE);

            cvExitGroup.setVisibility(View.VISIBLE);

            tvNoLongerMember.setVisibility(View.GONE);


        } else {
            tvAddParticipants.setVisibility(View.GONE);
            cvDeleteGroup.setVisibility(View.GONE);
            cvExitGroup.setVisibility(View.GONE);
            ivEdit.setVisibility(View.GONE);
            tvNoLongerMember.setVisibility(View.VISIBLE);
        }

        this.adapter = new GroupDetailsAdapter(this, this.memberList, this.roomJID, this.chatRoom.getSubject(), loggedInMember);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(GroupDetailsActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initOnclickListener() {

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupDetailsActivity.this.finish();
            }
        });

        cvExitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitGroupAlert();
            }
        });

        cvReportSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportSpamAlert();
            }
        });

        cvDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteGroupAlert();
            }
        });

        tvAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(GroupDetailsActivity.this,AddParticipantActivity.class);
                intent.putExtra(CommonConstants.JID, roomJID);
                startActivity(intent);
         }
        });

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupDetailsActivity.this, EditGroupName.class);
                intent.putExtra(CommonConstants.JID, roomJID);
                startActivity(intent);
            }
        });
    }


    private void showDeleteGroupAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupDetailsActivity.this);
        dialog.setMessage(" Are you sure want to delete this group?");
        dialog.setCancelable(true);

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                AndroidUtils.showToast(GroupDetailsActivity.this, " Deleting this group.");

                AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
                boolean deleted = userManager.destroyChatRoom(roomJID, "Owner want to delete it");

                if (deleted) {
                    AndroidUtils.showToast(GroupDetailsActivity.this, " Group " + chatRoom.getSubject() + " deleted successfully.");
                    // goBack(true, false);
                    refreshView();

                } else {
                    AndroidUtils.showToast(GroupDetailsActivity.this, " Something went wrong while deleting " + chatRoom.getSubject() + " group.");
                }

                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showReportSpamAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupDetailsActivity.this);
        dialog.setMessage("Report spam and leave this group?");
        dialog.setCancelable(true);

        dialog.setPositiveButton("REPORT AND LEAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                AndroidUtils.showToast(GroupDetailsActivity.this, "Report has been taken against this group.");

                AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
                userManager.sendLeaveChatRoomRequest(roomJID);

                //goBack(true, false);
                refreshView();

                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showExitGroupAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupDetailsActivity.this);
        dialog.setMessage("Exit " + this.chatRoom.getSubject() + " group?");
        dialog.setCancelable(true);

        dialog.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
                userManager.sendLeaveChatRoomRequest(roomJID);
                refreshView();
                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void refreshView() {
        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        this.chatRoom = userManager.getChatRoomDetails(this.roomJID);

        this.memberList = new ArrayList<>(this.chatRoom.getMembers());
        this.isGroupMember = this.chatRoom.isRoomMember(Platform.getInstance().getUserJID());

        setChatAdapter();
    }
}

