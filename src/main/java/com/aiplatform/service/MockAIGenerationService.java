package com.aiplatform.service;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MockAIGenerationService implements AIGenerationService {

    private static final List<String> RESPONSES = List.of(
            "El patrón Proxy introduce un objeto intermediario que comparte la interfaz del servicio real. Gracias a eso puede validar, contar o registrar cada llamada antes de delegarla, sin que el cliente note la diferencia y sin modificar una sola línea del servicio original.",
            "Una buena estrategia de rate limiting protege la plataforma de picos de tráfico. Al limitar las solicitudes por minuto según el plan, se garantiza un uso justo de los recursos y se evita que un solo usuario degrade la experiencia de los demás.",
            "Las cuotas mensuales de tokens permiten traducir el consumo de un modelo de lenguaje en algo medible. Cada solicitud descuenta tokens del prompt y de la respuesta, y cuando la cuota se agota el sistema responde con un 402 invitando a mejorar el plan.",
            "Encadenar proxies es como pasar por varios controles en un aeropuerto: primero se verifica que no vayas demasiado rápido, luego que tengas saldo suficiente, y solo entonces llegas al servicio real. Cada control tiene una única responsabilidad.",
            "Separar la lógica de control de acceso del servicio principal hace que el sistema sea más fácil de mantener. Si mañana se necesita agregar caché o auditoría, basta con crear un nuevo proxy y añadirlo a la cadena.",
            "Un modelo generativo predice el siguiente token a partir del contexto anterior. Por eso los prompts más largos consumen más tokens: el modelo debe procesar todo el texto de entrada antes de empezar a responder.",
            "La diferencia entre los planes FREE, PRO y ENTERPRISE no está en el servicio de IA, que es el mismo para todos, sino en los proxies que lo rodean. Esa es justamente la ventaja del patrón: cambiar las reglas sin tocar el núcleo.",
            "Para diseñar una API robusta conviene devolver códigos HTTP expresivos: 429 cuando se supera el ritmo permitido, con el encabezado Retry-After, y 402 cuando el usuario necesita ampliar su plan para seguir consumiendo el servicio."
    );

    private final long latencyMs;

    public MockAIGenerationService(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String text = RESPONSES.get(ThreadLocalRandom.current().nextInt(RESPONSES.size()));
        long promptTokens = TokenEstimator.estimate(request.prompt());
        long completionTokens = TokenEstimator.estimate(text);
        return new GenerationResponse(text, promptTokens, completionTokens, System.currentTimeMillis() - start)
                .passedThrough("MockAIGenerationService");
    }
}
