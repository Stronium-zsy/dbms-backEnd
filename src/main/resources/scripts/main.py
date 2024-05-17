from openai import OpenAI
import sys

def get_openai_response(systext,usermessage):
    try:
        # Initialize OpenAI client
        client = OpenAI(
                api_key='TECRqKFTjyFu4',
                base_url='https://ai.liaobots.work/v1'
            )

        # Initialize message list with system prompt
        messages = [
            {
                "role": "system",
                "content": "这是数据库的全部信息，"+systext+
                           "你只可以回答和这个数据库有关的问题，"
                           "禁止回答和这个数据库的信息以及和数据库不相关的问题"
                           "这个数据库中表是以xml文件的形式存储的，"
                           "用户可以询问你有关这个数据库中数据的任何信息。"
                           "返回结果的时候不要直接返回xml文件，"
                           "要以文字的方式描述出来"
                           "牢记，xml文件就是数据表，数据表就是xml文件"
                           "一定不要直接返回xml文件，要描述出来"
                           "用户可以提问关于这个数据库的任何信息，同时允许用户进行复杂的询问，你要一一解答"
                           "数据库都在databases目录下，users目录下存放的都是用户，要严格区分"
            }
        ]

        # Add user input to message list
        messages.append(
            {
                "role": "user",
                "content":usermessage
            }
        )

        # Get OpenAI response
        response = client.chat.completions.create(
            model="gpt-3.5-turbo-1106",
            messages=messages,
            stream=False,
        )

        # Return model response
        return response.choices[0].message.content
    except Exception as e:
        print("An error occurred:", e)
        return e

def main():
    if len(sys.argv) < 3:
        print("Usage: python script.py [input_text1] [input_text2]")
        return

    systext = sys.argv[1]
    usermessage = sys.argv[2]

    # Get response from OpenAI
    response = get_openai_response(systext, usermessage)

    # Print OpenAI response
    print(response.encode('utf-8').decode('utf-8'))

if __name__ == "__main__":
    main()

