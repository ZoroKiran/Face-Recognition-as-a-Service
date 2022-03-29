import csv

correct_results_path = 'clsresults.csv'
generated_results_path = 'clslines.txt'
answers = {}
lines = []

with open(correct_results_path, 'r') as csvfile:
    csvreader = csv.DictReader(csvfile)
    for row in csvreader:
        answers[int(row['Image'].split('_')[1])] = row['Results']
correct = 0
incorrect = 0
odd = True
with open(generated_results_path, 'r') as txtfile:
    for line in txtfile:
        if odd:
            current_file = int(line.split(' ')[0].split('.')[0].split('_')[1])
            odd = not odd
        else:
            current_result = line.split(' ')[-1].strip()
            odd = not odd
            correct_answer = answers[current_file]
            if current_result == correct_answer:
                result = 'Correct'
                correct += 1
            else:
                result = 'Incorrect'
                incorrect += 1
                # print(correct_answer)

            if answers[current_file]=='Gerry':
                print(
                    f'{result}! File {current_file}, expected answer {answers[current_file]}, got answer {current_result}')

print(f'Correct: {correct}')
print(f'Inorrect: {incorrect}')
print(correct / (incorrect + correct))
