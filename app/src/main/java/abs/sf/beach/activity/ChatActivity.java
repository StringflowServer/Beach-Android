package abs.sf.beach.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import abs.ixi.client.core.PacketCollector;
import abs.ixi.client.core.Platform;
import abs.ixi.client.core.Session;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.util.StringUtils;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.AckPacket;
import abs.ixi.client.xmpp.packet.Packet;
import abs.sf.beach.adapter.ChatAdapter;
import abs.sf.beach.android.R;
import abs.sf.beach.notification.NotificationGenerator;
import abs.sf.beach.utils.VerticalSpaceDecorator;
import abs.sf.client.android.db.DbManager;
import abs.sf.client.android.managers.AndroidChatManager;
import abs.sf.client.android.messaging.ChatLine;
import abs.sf.client.android.messaging.ChatLineReceiver;
import abs.sf.client.android.notification.fcm.SFFcmService;

public class ChatActivity extends StringflowActivity implements PacketCollector, ChatLineReceiver {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etMessage;
    private Button btnSend, btnCreatePoll;
    private ImageView ivBack, ivNext;
    private TextView tvHeader;
    private List<ChatLine> chatLines;
    private JID jid, mJid;

    private boolean isGroup;
    private final static int GROUP_DETAILS = 1;

    private AndroidChatManager chatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Chat activity on create");
        setContentView(R.layout.activity_chat);
        initView();
        setChatAdapter();
        initOnclickListener();

        this.chatManager = (AndroidChatManager) Platform.getInstance().getChatManager();
        subscribeForChatlineAndAck();
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivNext = (ImageView) findViewById(R.id.ivNext);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnCreatePoll = (Button) findViewById(R.id.btnCreatePoll);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        ivNext.setVisibility(View.INVISIBLE);
        jid = (JID) getIntent().getSerializableExtra("jid");
        tvHeader.setText(getIntent().getStringExtra("name"));

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Chat activity on resume");
        String from = getIntent().getStringExtra("from");
        if(!StringUtils.isNullOrEmpty(from) &&
                StringUtils.safeEquals(from, "NotificationUtils", false)){
            jid = (JID) getIntent().getSerializableExtra("jid");
            tvHeader.setText(jid.getNode());
        }

        setChatAdapter();
        NotificationGenerator.setChatActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Chat activity on stop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationGenerator.removeChatActivity();
        System.out.println("Chat activity on pause");
    }

    @Override
    protected void onDestroy() {
        unsubscibeForChatLineAndAck();
        super.onDestroy();
        System.out.println("Chat activity on destroy");
    }

    private void subscribeForChatlineAndAck() {
        this.chatManager.addChatLineReceiver(this);
        SFFcmService.addChatLineReceiver(this);
        this.chatManager.addPacketCollector(AckPacket.class, this);
    }

    private void unsubscibeForChatLineAndAck() {
        this.chatManager.removeChatLineReceiver(this);
        SFFcmService.removeChatLineReceiver(this);
        this.chatManager.removePacketCollector(AckPacket.class, this);
    }

    public JID getJID () {
        return this.jid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GROUP_DETAILS && resultCode == RESULT_OK){
            boolean isGroupDeleted = data.getBooleanExtra("isGroupDeleted", false);
            if(isGroupDeleted){
                ChatActivity.this.finish();
            }
            boolean isGroupMember = data.getBooleanExtra("isGroupMember", true);
            chatMemberViewsHideShowOperation(isGroupMember);
        }
    }

    private void setChatAdapter() {
        isGroup = DbManager.getInstance().isRosterGroup(jid.getBareJID());
        mJid = (JID) Platform.getInstance().getSession().get(Session.KEY_USER_JID);
        if(isGroup){
            boolean isGroupMember = DbManager.getInstance().isChatRoomMember(jid,mJid);
            String from = getIntent().getStringExtra("from");
            if(!StringUtils.isNullOrEmpty(from) &&
                    StringUtils.safeEquals(from, "UserSearch", false)){
                isGroupMember = true;
            }
            ivNext.setImageResource(R.mipmap.ic_info);
            ivNext.setVisibility(View.VISIBLE);
            chatMemberViewsHideShowOperation(isGroupMember);
            btnCreatePoll.setVisibility(View.VISIBLE);
        }else{
            ivNext.setVisibility(View.INVISIBLE);
            btnCreatePoll.setVisibility(View.GONE);
        }

        chatLines = DbManager.getInstance().fetchConversationChatlines(jid.getBareJID(), isGroup);

        adapter = new ChatAdapter(ChatActivity.this, chatLines, isGroup);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new VerticalSpaceDecorator(10));
        recyclerView.setAdapter(adapter);
        if (chatLines.size() > 0) {
            recyclerView.scrollToPosition(chatLines.size() - 1);
        }
    }

    private void initOnclickListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ChatActivity.this.isTaskRoot()) {
                    startActivity(new Intent(ChatActivity.this, ChatBaseActivity.class));
                }
                DbManager.getInstance().updateUnreadCount(jid.getBareJID());
                finish();
            }
        });

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGroup){
                    boolean isGroupMember = DbManager.getInstance().isChatRoomMember(jid,mJid);
                    Intent intent = new Intent(ChatActivity.this, GroupDetailsActivity.class);
                    intent.putExtra("jid", jid);
                    intent.putExtra("name", getIntent().getStringExtra("name"));
                    intent.putExtra("isGroupMember",isGroupMember);
                    startActivityForResult(intent,GROUP_DETAILS);
                }
            }
        });

        btnCreatePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, PollActivity.class);
                intent.putExtra("poll", "create");
                startActivity(intent);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessage.getText() == null || StringUtils.safeEquals(etMessage.getText().toString(), StringUtils.EMPTY)) {
                    return;
                }

                try {

                    ChatLine chatLine = chatManager.sendTextMessage(etMessage.getText().toString(),jid.getBareJID(),isGroup);
                    chatLines.add(chatLine);
                    etMessage.setText("");
                    adapter.notifyItemInserted(chatLines.size()-1);
                    recyclerView.scrollToPosition(chatLines.size() - 1);

                } catch (Exception e) {
                    //swallow
                }
            }
        });
    }

    private void chatMemberViewsHideShowOperation(boolean isGroupMember){
        if(!isGroupMember){
            etMessage.setHint(R.string.no_rec_msg);
            etMessage.setGravity(Gravity.CENTER);
            etMessage.setEnabled(false);
            btnSend.setVisibility(View.GONE);
        }else{
            etMessage.setEnabled(true);
            btnSend.setEnabled(true);
        }
    }

    @Override
    public void handleChatLine(final ChatLine chatLine) {
        if (StringUtils.safeEquals(chatLine.getPeerBareJid(), this.jid.getBareJID(), false)) {
            chatLines.add(chatLine);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(chatLines.size()-1);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public <T extends Packet> void collect(List<T> packets) {
        Iterator var2 = packets.iterator();

        while(var2.hasNext()) {
            Packet packet = (Packet)var2.next();
            this.collect(packet);
        }

    }

    @Override
    public void collect(Packet packet) {
        System.out.println("Entering chat actvity JID : " + this.jid.getBareJID() + " collect Packet for packet : " + packet);
        if (packet instanceof AckPacket) {
            AckPacket ack = (AckPacket) packet;
            List<String> messageIds = ack.getMessageIds();

            if (!CollectionUtils.isNullOrEmpty(messageIds)) {
                for (final String messageId : messageIds) {

                    for (ChatLine line : chatLines) {
                        if (StringUtils.safeEquals(line.getMessageId(), messageId)) {
                            line.setDeliveryStatus(1);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            System.out.println("gettting out from collect method chat actvity JID : " + this.jid.getBareJID() + " collect Packet for packet : " + packet);
        }

    }


    @Override
    public void onBackPressed() {
        if (ChatActivity.this.isTaskRoot()) {
            startActivity(new Intent(ChatActivity.this, ChatBaseActivity.class));
        }

        DbManager.getInstance().updateUnreadCount(jid.getBareJID());
        this.finish();
    }

}
