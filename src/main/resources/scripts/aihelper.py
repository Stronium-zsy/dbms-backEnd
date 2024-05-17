from openai import OpenAI
import sys

def get_openai_response(systext,usermessage):
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
                       "这个数据库中表是以xml文件的形式存储的，"
                       "你需要根据用户已经输出的不完整的sql语句根据数据库结构补充完整，并返回完整的sql语句"
                       "你只需要返回需要补全的部分"
                       "不可以返回文字描述，只需要返回未补全的sql语句"
                       "牢记，xml文件就是数据表，数据表就是xml文件"
                       "一定不要直接返回xml文件，要描述出来"
                       "数据库都在databases目录下，users目录下存放的都是用户，要严格区分"
                       "每次返回一条语句就可以，不需要返回多条，同时除了sql语句不要有其他内容"
                       "返回的结果不需要包裹在其他东西中，直接返回就可以"
                       "不要包裹在''''sql'''中，记住"
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

def main():
    if len(sys.argv) < 2:
        print("Usage: python script.py [input_text]")
        return


    systext=sys.argv[1]
    usermessage=sys.argv[2]

    # Get response from OpenAI
    response = get_openai_response(systext,usermessage)

    # Print OpenAI response
    print(response.encode('utf-8').decode('utf-8'))

if __name__ == "__main__":
    main()
