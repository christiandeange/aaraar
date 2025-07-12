/**
 * Copyright 2007 Google Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.christian.aaraar.shading.impl.jarjar.transform.jar;

import sh.christian.aaraar.shading.impl.jarjar.transform.Transformable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class JarProcessorChain extends ArrayList<JarProcessor> implements JarProcessor {

    public JarProcessorChain(@Nonnull Iterable<? extends JarProcessor> processors) {
        for (JarProcessor processor : processors)
            add(processor);
    }

    public JarProcessorChain(@Nonnull JarProcessor... processors) {
        this(Arrays.asList(processors));
    }

    @Override
    public Result scan(Transformable struct) throws IOException {
        for (JarProcessor processor : this)
            if (processor.scan(struct) == Result.DISCARD)
                return Result.DISCARD;
        return Result.KEEP;
    }

    @Override
    public Result process(Transformable struct) throws IOException {
        for (JarProcessor processor : this)
            if (processor.process(struct) == Result.DISCARD)
                return Result.DISCARD;
        return Result.KEEP;
    }
}
