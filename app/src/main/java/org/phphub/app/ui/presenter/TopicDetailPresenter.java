package org.phphub.app.ui.presenter;

import android.os.Bundle;

import com.github.pwittchen.prefser.library.Prefser;

import org.phphub.app.api.entity.TopicEntity;
import org.phphub.app.api.entity.element.Topic;
import org.phphub.app.common.base.BaseRxPresenter;
import org.phphub.app.model.TokenModel;
import org.phphub.app.model.TopicModel;
import org.phphub.app.ui.view.topic.TopicDetailsActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

public class TopicDetailPresenter extends BaseRxPresenter<TopicDetailsActivity> {
    private static final int REQUEST_TOPIC_ID = 1;

    int topicId;

    @Inject
    TopicModel topicModel;

    @Inject
    TokenModel tokenModel;

    @Inject
    Prefser prefser;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        restartableLatestCache(REQUEST_TOPIC_ID,
                new Func0<Observable<Topic>>() {
                    @Override
                    public Observable<Topic> call() {
                        return topicModel.getTopicDetailById(topicId)
                                .observeOn(AndroidSchedulers.mainThread())
                                .map(new Func1<TopicEntity.ATopic, Topic>() {
                                    @Override
                                    public Topic call(TopicEntity.ATopic topic) {
                                        return topic.getData();
                                    }
                                })
                                .compose(TopicDetailPresenter.this.<Topic>applyRetryByGuest(tokenModel, prefser));
                    }
                },
                new Action2<TopicDetailsActivity, Topic>() {
                    @Override
                    public void call(TopicDetailsActivity topicDetailsActivity, Topic topic) {
                        topicDetailsActivity.initView(topic);
                    }
                },
                new Action2<TopicDetailsActivity, Throwable>() {
                    @Override
                    public void call(TopicDetailsActivity topicDetailsActivity, Throwable throwable) {
                        topicDetailsActivity.onNetworkError(throwable);
                    }
                });
    }

    public void request(int topicId) {
        if (topicId > 0) {
            this.topicId = topicId;
            start(REQUEST_TOPIC_ID);
        }
    }
}