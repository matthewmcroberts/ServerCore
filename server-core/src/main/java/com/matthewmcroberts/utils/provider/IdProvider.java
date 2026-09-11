package com.matthewmcroberts.utils.provider;

import lombok.NonNull;

public interface IdProvider {
    @NonNull
    String getId();
}
