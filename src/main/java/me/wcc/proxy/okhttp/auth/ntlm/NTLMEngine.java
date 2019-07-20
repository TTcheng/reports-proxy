package me.wcc.proxy.okhttp.auth.ntlm;

/**
 * <strong>Copy from apache httpClient</strong>
 *
 * Abstract NTLM authentication engine. The engine can be used to
 * generate Type1 messages and Type3 messages in response to a
 * Type2 challenge.
 *
 * @since 4.0
 */
public interface NTLMEngine {

    /**
     * Generates a Type1 message given the domain and workstation.
     *
     * @param domain Optional Windows domain name. Can be {@code null}.
     * @param workstation Optional Windows workstation name. Can be
     *  {@code null}.
     * @return Type1 message
     * @throws NTLMEngineException
     */
    String generateType1Msg(
            String domain,
            String workstation) throws NTLMEngineException;

    /**
     * Generates a Type3 message given the user credentials and the
     * authentication challenge.
     *
     * @param username Windows user name
     * @param password Password
     * @param domain Windows domain name
     * @param workstation Windows workstation name
     * @param challenge Type2 challenge.
     * @return Type3 response.
     * @throws NTLMEngineException
     */
    String generateType3Msg(
            String username,
            String password,
            String domain,
            String workstation,
            String challenge) throws NTLMEngineException;

}
