package abs.sf.beach.fragment;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import abs.ixi.client.core.Platform;
import abs.ixi.client.util.CollectionUtils;
import abs.sf.beach.adapter.ConversationAdapter;
import abs.sf.beach.android.R;
import abs.sf.client.android.db.DbManager;
import abs.sf.client.android.managers.AndroidChatManager;
import abs.sf.client.android.messaging.ChatLine;
import abs.sf.client.android.messaging.ChatListener;
import abs.sf.client.android.messaging.Conversation;
import abs.sf.client.android.notification.fcm.SFFcmService;


/**
 * this fragment is for Conversations
 */
public class ConversationFragment extends Fragment implements ChatListener {
    private RecyclerView recyclerViewConversation;
    private List<Conversation> conversations;
    private ConversationAdapter adapter;
    private AndroidChatManager chatManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("Conersation fragment on Create");
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        initView(view);

        this.chatManager = (AndroidChatManager) Platform.getInstance().getChatManager();
        subscribeForChatline();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Conersation fragment on resume");
        this.conversations = DbManager.getInstance().fetchConversations();
        setConversationAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Conersation fragment on stop");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Conersation fragment on pause");
    }

    @Override
    public void onDestroy() {
        unsubscibeForChatLine();
        super.onDestroy();
        System.out.println("Conersation fragment on destroy");
    }

    private void subscribeForChatline() {
        this.chatManager.addChatListener(this);
        SFFcmService.addChatListener(this);
    }

    private void unsubscibeForChatLine() {
        this.chatManager.removeChatListener(this);
        SFFcmService.removeChatListener(this);
    }

    private void initView(View view) {
        recyclerViewConversation = (RecyclerView) view.findViewById(R.id.recyclerViewConversation);
    }

    private void setConversationAdapter() {
        if (!CollectionUtils.isNullOrEmpty(this.conversations)) {
            if(recyclerViewConversation!=null){
                recyclerViewConversation.setLayoutManager(new LinearLayoutManager(getActivity()));

                Collections.sort(this.conversations, new Comparator<Conversation>() {
                    @Override
                    public int compare(Conversation conversation, Conversation t1) {
                        return conversation.getLastUpdateTime() >= t1.getLastUpdateTime() ? -1 : 1;
                    }
                });

                adapter = new ConversationAdapter(getActivity(), conversations);
                recyclerViewConversation.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onChatLine(ChatLine chatLine) {
        this.conversations = DbManager.getInstance().fetchConversations();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setConversationAdapter();
            }
        });
    }

    @Override
    public void onServerAck(String s) {
        //Do nothing
    }

    @Override
    public void onCMDeliveryReceipt(String s) {
        //Do nothing
    }

    @Override
    public void onCMAcknowledgeReceipt(String s) {
        //Do nothing
    }

    @Override
    public void onCMDisplayedReceipt(String s) {
        //Do nothing
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            this.conversations = DbManager.getInstance().fetchConversations();
            setConversationAdapter();
        }
    }
}
