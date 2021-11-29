#include <jni.h>
#include <android/log.h>
#include <opus_multistream.h>
#include <string.h>
#include <memory.h>
#include <stddef.h>
#include <malloc.h>

OpusMSDecoder * g_decoder = NULL;
int32_t g_bitdepth = 0;
int32_t gn_bitdepth = 0;
int32_t gn_channels = 0;
int32_t gn_capacity = 0;
int32_t gn_framesize = 20;
uint8_t * g_pcm_buffer = NULL;

JNIEXPORT void JNICALL
Java_com_mixedtek_elastics_player_decoder_OPUSDecoder_create(JNIEnv *env, jclass clazz, jint samplerate, jint bitdepth, jint channels, jint streams, jint coupled, jstring map)
{
    if(g_decoder)
    {
        opus_multistream_decoder_destroy(g_decoder);
        g_decoder = NULL;
    }

    /*
    static const int32_t unknown = -1;
    static const int32_t pcm_16bit = 0;
    static const int32_t pcm_24bit = 1;
    static const int32_t pcm_32bit = 2;
    static const int32_t pcm_float = 3; */
    g_bitdepth = (int)bitdepth;
    if(g_bitdepth==0)
        gn_bitdepth = sizeof(int16_t);
    else if(g_bitdepth==3)
        gn_bitdepth = sizeof(float);

    gn_channels = (int)channels;

    gn_capacity = gn_framesize * (((int)samplerate) / 1000) * gn_channels * gn_bitdepth;
    g_pcm_buffer = malloc(gn_capacity);
    memset(g_pcm_buffer, 0x00, gn_capacity);

    unsigned char mapping[6] = { 0 };
    int32_t err = OPUS_OK;
    g_decoder = opus_multistream_decoder_create((int)samplerate, gn_channels, (int)streams, (int)coupled, mapping, &err);
    if(err!=OPUS_OK)
    {
        if(g_decoder)
        {
            opus_multistream_decoder_destroy(g_decoder);
        }
        g_decoder = NULL;
    }
}

JNIEXPORT jint JNICALL
Java_com_mixedtek_elastics_player_decoder_OPUSDecoder_decode(JNIEnv * env, jclass clazz, jbyteArray encoded, jint offset, jint length, jbyteArray pcm)
{
    uint8_t * buffer = (uint8_t*)malloc((int)length);

    (*env)->GetByteArrayRegion(env, encoded, (int)offset, (int)length, (jbyte*)buffer);

    int32_t nwritten = 0;
    if(g_bitdepth==3) //float
        nwritten = opus_multistream_decode_float(g_decoder, buffer, (int)length, g_pcm_buffer, gn_capacity, 0);
    else //short
        nwritten = opus_multistream_decode(g_decoder, buffer, (int)length, g_pcm_buffer, gn_capacity, 0);

    jint pcm_length = 0;
    if(nwritten>0)
    {
        pcm_length = nwritten * gn_channels * gn_bitdepth;
        (*env)->SetByteArrayRegion(env, pcm, 0, nwritten * gn_channels * gn_bitdepth, g_pcm_buffer);
    }

    if(buffer)
    {
        free(buffer);
        buffer = NULL;
    }
    return pcm_length;
}

JNIEXPORT void JNICALL
Java_com_mixedtek_elastics_player_decoder_OPUSDecoder_destroy(JNIEnv *env, jclass clazz) {
    if (g_decoder) {
        opus_multistream_decoder_destroy(g_decoder);
    }

    if (g_pcm_buffer)
    {
        free(g_pcm_buffer);
        g_pcm_buffer = NULL;
    }
}