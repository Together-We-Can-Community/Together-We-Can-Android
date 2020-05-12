/*
  Copyright (c) 2020, Arimac Lanka (PVT) Ltd.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.arimaclanka.android.wecan.bluetooth;

import java.util.UUID;

public class Consts {
    public static final UUID SERVICE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID READ_CHARACTERISTIC_UUID = UUID.fromString("00000000-0000-0000-1111-000000000000");
    public static final UUID WRITE_CHARACTERISTIC_UUID = UUID.fromString("00000000-0000-0000-2222-000000000000");
    public static final int SCAN_DURATION = 10 * 1000;
    public static final int SCAN_INTERVAL = 2 * 60 * 1000;
    public static final int DISCOVERY_TIMEOUT = 10 * 1000;
    public static final long ADV_PAYLOAD_LIFE = 15 * 60 * 1000;
}
