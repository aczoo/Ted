package com.example.ted;

import ai.api.model.AIError;
import ai.api.model.AIResponse;

public interface AIListener {

    void onResult(AIResponse result);

    void onError(AIError error);

    void onAudioLevel(float level);

    void onListeningStarted();

    void onListeningCanceled();

    void onListeningFinished();
}
