/**
 * Copyright 2016 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.cli.output;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class OutputPrinter {
    private final Logger logger;

    public OutputPrinter(Logger logger) {
        this.logger = logger;
    }

    @SuppressFBWarnings({"SLF4J_FORMAT_SHOULD_BE_CONST", "SLF4J_UNKNOWN_ARRAY"})
    public void info(String message, Object... args) {
        logger.info(message, args);
        System.out.println(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @SuppressFBWarnings({"SLF4J_FORMAT_SHOULD_BE_CONST", "SLF4J_UNKNOWN_ARRAY"})
    public void error(String message, Object... args) {
        logger.error(message, args);
        System.err.println(MessageFormatter.arrayFormat(message, args).getMessage());
    }
}
